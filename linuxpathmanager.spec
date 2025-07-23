Name:           linuxpathmanager
Version:        1.0
Release:        1%{?dist}
Summary:        GUI tool to manage PATH in Linux

License:        GPLv3+
URL:            https://github.com/djaquels/LinuxPathManager
Source0:        linuxpathmanager-1.0.tar.gz

BuildArch:      noarch
Requires:       java-latest-openjdk, openjfx

%description
A simple JavaFX-based utility to view and edit the system PATH and environment variables.

%prep
%setup -q

%build
# No compilation needed, just packaging

%install
mkdir -p %{buildroot}/usr/share/linuxpathmanager
mkdir -p %{buildroot}/usr/bin
mkdir -p %{buildroot}/usr/share/applications
cp -a linux-path-gui-1.0-SNAPSHOT.jar icon.png %{buildroot}/usr/share/linuxpathmanager/
install -m 755 linuxpathmanager %{buildroot}/usr/bin/linuxpathmanager
install -m 644 linuxpathmanager.desktop %{buildroot}/usr/share/applications/linuxpathmanager.desktop

%files
/usr/bin/linuxpathmanager
/usr/share/linuxpathmanager/linux-path-gui-1.0-SNAPSHOT.jar
/usr/share/linuxpathmanager/icon.png
/usr/share/applications/linuxpathmanager.desktop

%changelog
* Mon Jul 07 2025 Hector Jacales <daniel.jacales@outlook.com> 1.0-1
- Initial RPM release