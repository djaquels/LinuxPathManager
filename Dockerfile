# Fedora 42 RPM Build Environment for LinuxPathManager

FROM fedora:42

# Install build tools and Java dependencies for the package
RUN dnf -y update && \
    dnf -y install rpm-build rpmdevtools java-latest-openjdk-devel openjfx tar && \
    dnf clean all

# Create a build user (for security, don't build as root)
RUN useradd -m builder

USER builder
WORKDIR /home/builder

# Set up rpmbuild tree
RUN rpmdev-setuptree

# Copy spec and sources into container
# (You will COPY your files in build context)
# Place your linuxpathmanager.spec and source tarball in the same directory as this Dockerfile
COPY linuxpathmanager.spec /home/builder/rpmbuild/SPECS/
COPY linuxpathmanager-1.0.tar.gz /home/builder/rpmbuild/SOURCES/

# Build the RPM (run at build-time for demonstration, or at runtime for more flexibility)
# You can comment out RUN rpmbuild... below if you want to run manually in container
RUN rpmbuild -ba /home/builder/rpmbuild/SPECS/linuxpathmanager.spec

# Output directory for RPMs
# When container is run, you can copy from /home/builder/rpmbuild/RPMS/noarch/
# Or mount a volume to /home/builder/rpmbuild/RPMS to retrieve built RPMs

# Default command: print location of RPMs (override with bash for interactive)
CMD echo "Your RPM(s) are in /home/builder/rpmbuild/RPMS/noarch/"