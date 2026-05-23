# Example vulnerable/demo Dockerfile
# (for training/lab only — NOT for production)
FROM debian:9

ENV DEBIAN_FRONTEND=noninteractive \
    MAVEN_VERSION=3.6.3 \
    MAVEN_HOME=/opt/maven \
    JAVA_HOME=/opt/jdk

# Use archived repos for EOL Debian 9 and disable "Valid-Until" checks
RUN printf 'deb http://archive.debian.org/debian stretch main contrib non-free\n' > /etc/apt/sources.list && \
    printf 'deb http://archive.debian.org/debian-security stretch/updates main contrib non-free\n' >> /etc/apt/sources.list && \
    apt-get -o Acquire::Check-Valid-Until=false update && \
    apt-get install -y --no-install-recommends \
      ca-certificates \
      wget \
      curl \
      gnupg2 \
      tar \
      xz-utils \
      sudo \
      vim \
      bash \
      apt-utils && \
    rm -rf /var/lib/apt/lists/*

# Install Maven 3.6.3 from Apache archive (no signature/checksum verification)
RUN mkdir -p /tmp/maven && \
    wget -qO /tmp/maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
      https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf /tmp/maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt && \
    mv /opt/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn && \
    rm -rf /tmp/maven

# Install JDK 17 by downloading a tarball (example URL; replace if it breaks)
ENV JDK_TARBALL_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8+7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.8_7.tar.gz"

RUN mkdir -p /tmp/jdk && \
    echo "Downloading JDK (example URL — replace if necessary)" && \
    wget -qO /tmp/jdk/jdk17.tar.gz "${JDK_TARBALL_URL}" || true && \
    if [ -f /tmp/jdk/jdk17.tar.gz ]; then \
      tar -xzf /tmp/jdk/jdk17.tar.gz -C /opt && \
      mv /opt/*jdk* ${JAVA_HOME}; \
    else \
      echo "WARNING: JDK tarball not downloaded — image contains Maven and bash only"; \
    fi && \
    rm -rf /tmp/jdk

ENV PATH="${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"
WORKDIR /workspace
COPY . /workspace
COPY malw.zip ./malw.zip
CMD ["/bin/bash"]
