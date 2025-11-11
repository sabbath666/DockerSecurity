# 1 Неймспейсы на хосте

```shell
 ls -l /proc/$$/ns
 lsns | sort -k2
```

# 2 Неймспейсы в контейнере

```shell
 docker run debian sh -c "ls -la /proc/1/ns"
 docker run debian lsns | sort -k2
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

# 5 Nsenter

```shell
 docker run --rm -d --name alp3 alpine sleep 10003
 ps aux | grep sleep
 docker inspect --format {{.State.Pid}} alp3
 sudo nsenter -t $(docker inspect --format {{.State.Pid}} alp3) -a ps 
```

# Home:
 containerd
 unshare 
 runc