# Contributing

## Running the Extension

With Visual Studio Code:

- Clone this repository locally.
- Run `npm install` in the cloned `codewind-java-profiler` folder. This installs all necessary npm modules in the client directory.
- Open the clone of this repository in Visual Studio Code.
- Press Ctrl+Shift+B (Cmd+Shift+B on Mac) to compile the client.
- Switch to the Debug viewlet.
- Select `Launch Client` from the drop down and press the Run icon.

## Testing

### Integration Tests

To be added

### Server Tests

Unit tests for the Java Server are in the `server/src/test` directory. These are JUnit unit tests, but can only be run inside the server Docker container.

To run the container complete the following steps:

- Run `docker build -t java-ls .` in the `server` directory.
- Run `docker run -it java-ls bash`.
- In the `/profiling` directory, run `mvn test`.
- You will see a summary of all tests run.

## Building/Installing the Extension

To build a `.vsix` extension package that can then be installed/published:

- Run `npm install` in the `codewind-java-profiler` folder.
- Install the `vsce` package globally with `npm install -g vsce`.
- Run `vsce package` in the `codewind-java-profiler` folder.
- A `.vsix` file will then be generated.

To install the extension:

- Run `code --install-extension <name of generated vsix file>` in the `codewind-java-profiler` folder.
- Restart Visual Studio Code.
- The extension should appear in your list of installed extensions.

For more information refer to: <https://code.visualstudio.com/api/working-with-extensions/publishing-extension>
