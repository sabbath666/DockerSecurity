# 1 Ставим capsh

```shell
sudo apt-get install libcap2-bin
```

# 2 Capabilities root на хосте

```shell
sudo capsh --print | grep 'Bounding set'
sudo capsh --print | grep 'Bounding set' | tr ',' '\n' | wc -l
```

# 3 Capabilities root в контейнере

```shell
docker run alpine sh -c "apk add -q libcap && capsh --print|grep 'Bounding set'"
docker run debian sh -c "apt-get update && apt install libcap2-bin -y && capsh --print|grep 'Bounding set'|tr ',' '\n'|wc -l"
```
