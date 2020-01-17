# Codewind Java Profiler

Annotates your Java code with code highlighting for your hottest methods in your Eclipse Codewind projects.

![platforms](https://img.shields.io/badge/runtime-Java-yellow.svg)
[![License](https://img.shields.io/badge/License-EPL%202.0-red.svg?label=license&logo=eclipse)](https://www.eclipse.org/legal/epl-2.0/)

This extension provides code highlighting showing relative time spent in Java methods based on profiling data gathered through Codewind's load testing feature.

## Usage

### Prerequisites

- The Eclipse Codewind extension (available [here](https://marketplace.visualstudio.com/items?itemName=IBM.codewind)) installed in Visual Studio Code.
- A Java Liberty project bound to Codewind.

With Visual Studio Code:

- Open your Java Liberty Project's Performance Dashboard by right-clicking on the project in the Codewind section of Visual Studio Code and selecting `Open Performance Dashboard`.
- Once the Performance Dashboard opens, click `Run Load Test`.
- Once the test has completed, it will create profiling data in a `load-test/[timestamp]/xxxx.hcd` file in your Codewind project.
- In Visual Studio Code open a Java file in your project.
- The extension will highlight any methods which were found in the profiling data and annotate them to show the percentage of time they were running on the CPU during profiling.

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
