# 1 Trivy

```shell
docker run --rm  -v /var/run/docker.sock:/var/run/docker.sock   aquasec/trivy -f json image fb1|jq
```

# 2 Clamav
```shell
 docker run --rm -v $(pwd):/scan clamav/clamav clamscan -r /scan
```


# 3 Shellcheck

```shell
alias shellcheck='docker run --rm -it -v "$(pwd):/mnt" koalaman/shellcheck:stable'
```

# GPT