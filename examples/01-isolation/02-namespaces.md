# 1 Неймспейсы на хосте

```shell
ls -l /proc/$$/ns
lsns
```

# 2 Неймспейсы в контейнере

```shell
docker run debian sh -c "ls -la /proc/1/ns"
docker run debian lsns
```

# 3 PID в контейнере

```shell
docker run --rm -d --name alp1 alpine sleep 10001
docker run --rm -d --name alp2 alpine sleep 10002
docker exec alp1 ps aux
docker exec alp2 ps aux
```

# 4 PID на хосте

```shell
ps aux | grep sleep
```

# 5 Сеть

```shell
docker run --network=host -it alpine ash
apk add curl jq
curl -X GET http://127.0.0.1:2375/containers/json?all=true
```

