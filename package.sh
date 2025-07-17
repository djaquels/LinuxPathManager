#!/bin/bash
#bygga .jar
mvn clean package
#.deb folder struktur
mkdir -p linuxpathmanager
mkdir -p linuxpathmanager/debian
mkdir -p linuxpathmanager/usr/bin
mkdir -p linuxpathmanager/usr/share/linuxpathmanager
mkdir -p linuxpathmanager/usr/share/applications
# skapa control fil
cp control linuxpathmanager/debian
cp copyright linuxpathmanager/debian
cp rules linuxpathmanager/debian
cp install linuxpathmanager/debian
cp exec linuxpathmanager/usr/bin/linuxpathmanager
chmod +x linuxpathmanager/usr/bin/linuxpathmanager
# kopiera .jar och iconer
cp target/linux-path-gui-1.0-SNAPSHOT.jar linuxpathmanager/usr/share/linuxpathmanager/
cp icon.png linuxpathmanager/usr/share/linuxpathmanager/
cp config.json linuxpathmanager/usr/share/linuxpathmanager/
chmod 666 linuxpathmanager/usr/share/linuxpathmanager/config.json
# desktop fil
cp linuxpathmanager.desktop linuxpathmanager/usr/share/applications/linuxpathmanager.desktop
# bygg .deb packet
cd linuuxpathmanager && debuild -S -sa -k$KEY_ID
