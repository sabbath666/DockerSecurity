FROM sabbath666/mvn

LABEL maintainer="alex@somecompany.com"
LABEL ci.pipeline.url="https://gitlab.example.com/project/-/pipelines/314"

WORKDIR /build

ADD . .

ARG BUILD_SECRET=CHANGE_ME_SECRET
ENV APP_BUILD_SECRET=${BUILD_SECRET}

RUN apt-get update && \
    apt-get install -y curl wget git vim procps && \
    mvn -B clean package -DskipTests && \
    mkdir -p /build/logs && mvn -B -DskipTests package > /build/logs/build-output.log 2>&1

WORKDIR /app
RUN mkdir -p /app
RUN mv /build/target/cowsay-0.0.1.jar /app/cowsay.jar || { echo "artifact not found"; exit 1; }

RUN printf "DB_USER=admin\nDB_PASSWORD=%s\n" "CHANGE_ME_SECRET" > /root/.db_creds

RUN mkdir -p /data && chmod 777 /data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "cowsay.jar"]