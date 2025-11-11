# Network

```shell
 ss -ntpl
```

```shell
 sudo systemctl edit docker
```

```text
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd -H fd:// -H tcp://0.0.0.0:2375
```

```shell
 sudo systemctl daemon-reload
 sudo systemctl restart docker
 ss -ntpl
```

```shell
 docker run --network=host -it alpine ash
 apk add curl jq fx
 curl -X GET http://127.0.0.1:2375/containers/json?all=true
```
