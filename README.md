# Codewind Java Profiler

Annotates your Java code with code highlighting for your hottest methods in your Eclipse Codewind projects.

![platforms](https://img.shields.io/badge/runtime-Java-yellow.svg)
[![Eclipse License](https://img.shields.io/badge/license-Eclipse-brightgreen.svg)](https://github.com/eclipse/codewind-java-profiler/blob/master/LICENSE)
[![License](https://img.shields.io/badge/License-EPL%202.0-red.svg?label=license&logo=eclipse)](https://www.eclipse.org/legal/epl-2.0/)
[EPL 2.0](https://github.com/eclipse/codewind-java-profiler/blob/master/LICENSE)

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

## Contributing
Submit issues and contributions:
1. [Submitting issues](https://github.com/eclipse/codewind-java-profiler/issues)
2. [Contributing](CONTRIBUTING.md)
