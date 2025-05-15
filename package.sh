#!/bin/bash
#bygga .jar
mvn clean package
#.deb folder struktur
mkdir -p linuxpathmanager
mkdir -p linuxpathmanager/DEBIAN
mkdir -p linuxpathmanager/usr/bin
mkdir -p linuxpathmanager/usr/share/linuxpathmanager
mkdir -p linuxpathmanager/usr/share/applications
# skapa control fil
cp control linuxpathmanager/DEBIAN
cp exec linuxpathmanager/usr/bin/linuxpathmanager
chmod +x linuxpathmanager/usr/bin/linuxpathmanager
# kopiera .jar och iconer
cp target/linux-path-gui-1.0-SNAPSHOT.jar linuxpathmanager/usr/share/linuxpathmanager/
cp icon.png linuxpathmanager/usr/share/linuxpathmanager/  # Om du har en ikon
# desktop fil
cp linuxpathmanager.desktop linuxpathmanager/usr/share/applications/linuxpathmanager.desktop:
# bygg .deb packet
dpkg-deb --build linuxpathmanager