/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import * as path from 'path';
import * as fs from 'fs';
import * as net from 'net';
import { 
    workspace,
    ExtensionContext,
    WorkspaceFolder
} from 'vscode';
import {
	LanguageClient,
	LanguageClientOptions,
	StreamInfo,
} from 'vscode-languageclient';
import { promisify } from 'util';

import * as tarfs from 'tar-fs';
import * as Docker from 'dockerode';
import { URL } from 'url';

const docker = new Docker();

const followProgress = promisify(docker.modem.followProgress);

const clientPort: number = 3333;
const clientHost: string = 'host.docker.internal';
const codewindVolumeName: string = 'codewind_cw-workspace';
const dockerWorkspaceMountDir: string = '/profiling/workspaces';
const dockerRepo: string = 'ibmcom';
const dockerImage: string = 'codewind-java-profiler-language-server';
const dockerTag: string = 'latest';
const dockerFullImageName = `${dockerRepo}/${dockerImage}:${dockerTag}`;
const onWin: boolean = (process.platform === 'win32' ? true : false);
let clientServer: net.Server;
let client: LanguageClient;
let serverConnected = false;
let connectionSocket;

async function workspaceFolderToDockerBind(wsFolder: WorkspaceFolder): Promise<string> {
    let folderUriString = wsFolder.uri.toString(true).replace('file://', '');
    if (onWin) {
        folderUriString = folderUriString.replace(':', '');
    }
    // check if we're attempting to bind a project folder or a parent directory
    const wsFolderURL = new URL(wsFolder.uri.toString(true));
    const wsFolderContents = fs.readdirSync(wsFolderURL);
    console.log("wsFolderContents = " + wsFolderContents);
    if (wsFolderContents.includes('.cw-settings')) {
        // project folder
        return `${folderUriString}:${dockerWorkspaceMountDir}/${wsFolder.name}`;
    }
    // parent directory
    return `${folderUriString}:${dockerWorkspaceMountDir}`;
}

export async function activate(context: ExtensionContext) {

	// start socket server that the container connects to
	clientServer = net.createServer();
	clientServer.maxConnections = 1;
	clientServer.listen(clientPort);

	// start docker container
	const dockerBinds = await Promise.all(workspace.workspaceFolders.map(workspaceFolderToDockerBind));
	dockerBinds.forEach(a => console.log(a));

	let serverOptions = () => startServerDockerContainer(dockerBinds);

	// Options to control the language client
	let clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: [{ scheme: 'file', language: 'java' }],
		synchronize: {
			// Notify the server about file changes to '.hdc files contained in the workspace
			fileEvents: workspace.createFileSystemWatcher('**/*.hcd')
		}
	};

	// Create the language client and start the client.
	client = new LanguageClient(
		'codewindJavaProfiler',
		'Codewind Java Profiler',
		serverOptions,
		clientOptions
	);

	// Start the client. This will also launch the server
	client.start();
}

async function startServerDockerContainer(dockerBinds: string[]) {
	try {
		await removeExistingContainer();
	} catch (err) {
		// don't care if already removed
	}

	try {
		console.log(`Trying to pull image ${dockerFullImageName}`);

		await pullDockerImage();
		console.log('Pull completed!');
	} catch (error) {
		console.log('Pull failed, building from local Dockerfile');
		await buildLocalDockerImage();
		console.log(error);
    }
    await startContainer(dockerBinds);

    setupConnectionListeners();
    const serverSocket = await waitForServerConnection();

	// Connect to language server via socket
	let result: StreamInfo = {
			writer: serverSocket,
			reader: serverSocket
	};
	return Promise.resolve(result);
}

function waitForServerConnection() {
	return new Promise<net.Socket>(async (resolve, reject) => {
		const timeout = 30;
		let currentTime = 0;

		console.log(`Waiting for server connection on port ${clientPort}`);

		while(!serverConnected) {
			if(currentTime >= timeout) {
				reject(`Server didn't connect in ${timeout} seconds`);
			}

			await sleep(1000);

			currentTime ++;
		}

		resolve(connectionSocket);
	});
}

async function pullDockerImage() {
	const stream = await docker.pull(dockerFullImageName, {});
	// wait for the pull to complete
	await followProgress(stream);
}

async function buildLocalDockerImage() {
	const pack = tarfs.pack(path.join(__dirname, '../..', 'server'));

	const stream = await docker.buildImage(pack, {t: dockerFullImageName});
	// wait for the build to finish
	await new Promise((resolve, reject) => {
		docker.modem.followProgress(stream, (err: any, res: {} | PromiseLike<{}>) => err ? reject(err) : resolve(res), (event) => {
			console.log(event.stream);
		});
	});
}

async function removeExistingContainer() {
	let originalContainer: Docker.Container;
	originalContainer = await docker.getContainer(dockerImage);
	try {
		await originalContainer.stop();
	} catch (error) {
		// don't care if already stopped
	}
	await originalContainer.remove();
}

async function startContainer(dockerBinds: string[]) {
	const container = await docker.createContainer({
		Image: dockerFullImageName,
		name: dockerImage,
		Env: [`CLIENT_PORT=${clientPort}`, `CLIENT_HOST=${clientHost}`, `BINDS="${dockerBinds}"`, `WINDOWS_HOST=${onWin}`],
		HostConfig: {
			Binds: [ `${codewindVolumeName}:${dockerWorkspaceMountDir}` ]
		}
	});

	container.start();
}
function setupConnectionListeners() {
	// wait for the language server to connect
	clientServer.setMaxListeners(1);
	clientServer.on('connection', socket => {
		console.log('Language Server connected');
		serverConnected = true;
		connectionSocket = socket;

		socket.on('close', (hadError) => {
			console.log('Language Server disconnected' + (hadError ? 'with error.' : '.'));
			serverConnected = false;
			connectionSocket = null;

			clientServer.removeAllListeners();
		});
	});
}

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}
