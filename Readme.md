# Linux Path Manager
A simple linux widget to manage your paths. Inspired by the Windows Path Manager
this functionality is not available in Linux by default. This project aims to fulfill this user experience.


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
pending
