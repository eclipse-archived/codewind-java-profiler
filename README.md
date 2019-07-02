# Codewind Language Server for Java Profiling

![platforms](https://img.shields.io/badge/runtime-Java-yellow.svg)
[![Eclipse License](https://img.shields.io/badge/license-Eclipse-brightgreen.svg)](https://github.com/eclipse/codewind-java-profiler/blob/master/LICENSE)
[![License](https://img.shields.io/badge/License-EPL%202.0-red.svg?label=license&logo=eclipse)](https://www.eclipse.org/legal/epl-2.0/)
[EPL 2.0](https://github.com/eclipse/codewind-java-profiler/blob/master/LICENSE)

The Codewind language server for Java profiling annotates your Java code with code highlighting. By using profiling data gathered through Codewind load testing, the highlighting shows the relative time that is spent in JavaScript functions.

## Running the extension with Visual Studio Code (VS Code)
1. Open a local project that you created with [Codewind](https://microclimate-dev2ops.github.io/installlocally) and profiled by using the [performance test](https://microclimate-dev2ops.github.io/performancetesting#performance-testing-your-project) feature.
2. Opening the project creates profiling data in a `load-test/[timestamp]/<file_name>.hcd` file in your Codewind project.
3. In VS Code, open a Java file in your project. The extension highlights any lines that it finds in the profiling data and annotates them to show the percentage of time they were running on the CPU during profiling.

## Contributing
Submit issues and contributions:
1. [Submitting issues](https://github.com/eclipse/codewind-java-profiler/issues)
2. [Contributing](CONTRIBUTING.md)
