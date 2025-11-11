# Ограничение по ресурсам

```shell
 docker run --rm -m 256m -d --name stress debian sh -c "apt-get update && apt install stress -y && stress --vm 1 --vm-bytes 64M"
```

