# MCLib
[![GitHub version](https://img.shields.io/badge/release-1.7.2.6-blue)](https://search.maven.org/search?q=mclib)
[![GitHub stars](https://img.shields.io/github/stars/MultiCoreNetwork/MCLib)](https://github.com/MultiCoreNetwork/MCLib)
[![GitHub issues](https://img.shields.io/github/issues/MultiCoreNetwork/MCLib)](https://github.com/MultiCoreNetwork/MCLib/issues)

### Common library for Java programming
This library contains a bunch of frequently used code snippets and classes that will
help you work faster without wasting your time with always the same codes.

## Features
- [x] SQL libraries (Include MySQL & SQLite [WIP])
    - Database connection
    - Connection pool
    - Automatic reconnection
    - Table creation and alteration
    - Pre-made getters and setters to set and retrieve data to/from database
    - Custom queries
    - Easy method to close ResultSets, Statements and Connections
- [x] YAML library
    - Creation, reading and management of [YAML](https://yaml.org/) files
- [x] Console colors
    - Print to console with text alterations easily
    - Translate a string with color codes (&a, &b, &c, ...) to a colored string
- [x] Encryption
    - File encryption
    - Password hashing
- [x] Plugin system (Seed System) [WIP]
- [x] Misc
    - Zip utilities (Zip, Unzip, Append)
    - Object serialization / deserialization
    - 3D space methods (distance between two points, point in region)
    - Watchdog
- [x] Networking
    - Custom packet support
    - Netty connection pipeline

## Requirements
- You need at least JDK8.

## Getting started
You can add the modules using maven:
```xml
<dependency>
    <groupId>it.multicoredev.mclib</groupId>
    <artifactId>MCLib-MODULE</artifactId>
    <version>...</version>
    <scope>compile</scope>
</dependency>
```

Valid modules are:
  - Configurations
  - Console
  - Database
  - Network
  - Plugins
  - Utilities

You can also download the latest version of the library (with also sources and javadocs) from [here](https://multicoredev.it/job/MCLib/) and import it as a library.

## Contributing
To contribute to this repository just fork this repository make your changes or add your code and make a pull request.

## License
MCLib is released under "The 3-Clause BSD License". You can find a copy [here](https://github.com/MultiCoreNetwork/MCLib/blob/master/LICENSE)