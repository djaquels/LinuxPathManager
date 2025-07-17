# Linux Path Manager
**LinuxPathManager** JavaFX-based Linux desktop application to easy manage and edit system environment variables like PATH, JAVA_HOME, etc.
using a simple GUI. Ideal for developers, sysadmins, or Linux users who prefer a visual alternative,

## Local Development
### Install dependencies
mvn clean
### Execute
mvn exec:java -Dexec.mainClass="com.github.djaquels.Main"

## Installation
## From source
Run the package script in the root directory:
```bash
./package.sh
```
this will generate a linuxpathmanager.deb file in the root directory. Therefore you can install it with:
```bash
sudo apt install ./linuxpathmanager.deb
```

## From ppa
[Now available](https://launchpad.net/~hjacales-pro/+archive/ubuntu/hippo-systems)

## Features

- View and edit system environment variables (user and system level)
- Add, remove, and modify PATH entries
- Automatic detection of existing environment variables
- '.deb' package for easy installation (other Linux packkage managers pending)
- Remote (ssh) management of environment variables, for GUI server management (pending)

## Feedback and contributions

Feel free to open issues or pull requests, bug reports and enhancement requests are welcome.

