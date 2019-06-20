# Codewind Java Profiler

Annotates your Java code with code highlighting for your hottest methods in your Codewind projects.

![platforms](https://img.shields.io/badge/runtime-Java-yellow.svg)
[![Eclipse License](https://img.shields.io/badge/license-Eclipse-brightgreen.svg)](https://github.com/eclipse/codewind-java-profiler/blob/master/LICENSE)

This extension provides code highlighting showing relative time spent in Java methods based on profiling data gathered through Codewind Load Testing.

## Usage

### Prerequisites

- The Codewind extension (available [here](https://marketplace.visualstudio.com/items?itemName=IBM.codewind)) installed in Visual Studio Code.
- A Java Liberty project bound to Codewind.

With Visual Studio Code:

- Open your Java Liberty Project's Performance Dashboard by right-clicking on the project in the Codewind section of Visual Studio Code and selecting `Open Performance Dashboard`.
- Once the test has completed, it will create profiling data in a `load-test/[timestamp]/xxxx.hcd` file in your Codewind project.
- In Visual Studio Code open a Java file in your project.
- The extension will highlight any methods which were found in the profiling data and annotate them to show the percentage of time they were running on the CPU during profiling.

## Contributing

We welcome submitting issues and contributions.

1. [Submitting bugs](https://github.com/eclipse/codewind-java-profiler/issues)
2. [Contributing](CONTRIBUTING.md)

## License

[EPL 2.0](https://github.com/eclipse/codewind-java-profiler/blob/master/LICENSE)
