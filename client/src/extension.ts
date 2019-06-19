/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

import * as path from 'path';
import * as net from 'net';
import { workspace, ExtensionContext } from 'vscode';
import {
	LanguageClient,
	LanguageClientOptions,
	StreamInfo,
} from 'vscode-languageclient';

import * as tarfs from 'tar-fs';
import * as Docker from 'dockerode';
import * as ip from 'ip';

const docker = new Docker();
const clientPort: number = 3333;
const dockerImage: string = 'java-ls';
let clientServer: net.Server;
let client: LanguageClient;
let serverConnected = false;
let connectionSocket;

export async function activate(context: ExtensionContext) {

	// start socket server that the container connects to
	clientServer = net.createServer();
	clientServer.maxConnections = 1;
	clientServer.listen(clientPort);

	// start docker container
	const dockerBinds = workspace.workspaceFolders.map(wsFolder => `${wsFolder.uri.toString().replace('file://', '')}:/profiling/${wsFolder.name}`);
	dockerBinds.forEach(a => console.log(a));

	let serverOptions = () => startServerDockerContainer(dockerBinds);

	// Options to control the language client
	let clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: [{ scheme: 'file', language: 'java' }],
		synchronize: {
			// Notify the server about file changes to '.hdc files contained in the workspace
			fileEvents: workspace.createFileSystemWatcher('**/*.hdc')
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
	if(!process.env.REMOTE_SERVER) {
		let originalContainer: Docker.Container;
		try {
			originalContainer = await docker.getContainer(dockerImage);
			try {
				await originalContainer.stop();
			} catch (error) {
				// don't care if already stopped
			}
			await originalContainer.remove();
		} catch (error) {
			// if it doesn't exist then try to build it
			// TODO: host image and try to pull instead
			const pack = tarfs.pack(path.join(__dirname, '../..', 'server'));

			const stream = await docker.buildImage(pack, {t: dockerImage});
			// wait for the build to finish
			await new Promise((resolve, reject) => {
				docker.modem.followProgress(stream, (err: any, res: {} | PromiseLike<{}>) => err ? reject(err) : resolve(res), (event) => {
					// console.log(event.stream);
				});
			});
		}

		const container = await docker.createContainer({
			Image: dockerImage,
			name: dockerImage,
			Env: [`CLIENT_PORT=${clientPort}`, `CLIENT_HOST=${ip.address()}`, `BINDS="${dockerBinds}"`],
			HostConfig: {
				Binds: dockerBinds
			}
		});

		container.start();
	}

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
