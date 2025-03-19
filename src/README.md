# Docker Security

Контейнеризация уверенно заняла свое место в мире DevOps. Однако безопасность Docker-контейнеров остается актуальной проблемой. И далеко не каждый инженер знает как правильно и безопасно готовить, поставлять, развертывать и следить за контейнерами в рантайме. Сейчас копнем чуть глубже и разберемся как харденить Docker, но сначала вот вопрос: рут в контейнере и рут на хосте - это тот же рут или нет? А чем отличается и отличается ли? Если ты задумался, то тебе сюда.

<добавить агенду>

## Изоляция

Контейнеризация - это не виртуализация. Контейнер - это просто обычный процесс, который запускается в изолированной среде, но это не какая-то отдельная виртуальная машина. Процесс в контейнере - это процесс на хосте, который изолирован с помощью стандартных механизмов в Linux: cgroups2, namespaces и системный вызов [pivot root](https://tbhaxor.com/pivot-root-vs-chroot-for-containers/) (который пришел на смену chroot, как более надежный).

Что ж начнем с изоляции и рассмотрим несколько поучительных примеров.

### Пример 1: Root на хосте и root в контейнере: кто мощнее?

Сравним неймспейсы в хосте и на контейнере.
Для вывода неймспейсов на хосте выполним: 

```shell
ls -l /proc/$$/ns
```
```
total 0
lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 cgroup -> 'cgroup:[4026531835]'
lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 ipc -> 'ipc:[4026531839]'
lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 mnt -> 'mnt:[4026531841]'
lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 net -> 'net:[4026531840]'
lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 pid -> 'pid:[4026531836]'
lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 pid_for_children -> 'pid:[4026531836]'
--------------------------------------------------------------------------------------
| lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 time -> 'time:[4026531834]'                |
| lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 time_for_children -> 'time:[4026531834]'   |
| lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 user -> 'user:[4026531837]'                |
--------------------------------------------------------------------------------------
lrwxrwxrwx 1 admin admin 0 Mar 10 13:19 uts -> 'uts:[4026531838]'
```
<details>
  <summary>
объяснение команды
</summary>
выводит список пространств имён (namespaces), которые используются текущим процессом ($$ — это идентификатор текущего процесса в bash).

Что именно происходит:

•	/proc — это виртуальная файловая система Linux, содержащая информацию о процессах и других аспектах работы ядра.

•	$$ — переменная окружения в bash, которая хранит PID текущего shell-процесса.

•	/proc/[pid]/ns — каталог, в котором находятся символические ссылки на различные пространства имён (namespaces), используемые конкретным процессом.

</details>

Теперь посмотрим неймспейсы в контейнере:
```shell
docker run alpine ls -l /proc/1/ns
```
```
total 0
lrwxrwxrwx    1 root     root             0 Mar 10 14:20 cgroup -> cgroup:[4026533469]
lrwxrwxrwx    1 root     root             0 Mar 10 14:20 ipc -> ipc:[4026533467]
lrwxrwxrwx    1 root     root             0 Mar 10 14:20 mnt -> mnt:[4026533465]
lrwxrwxrwx    1 root     root             0 Mar 10 14:20 net -> net:[4026533470]
lrwxrwxrwx    1 root     root             0 Mar 10 14:20 pid -> pid:[4026533468]
lrwxrwxrwx    1 root     root             0 Mar 10 14:20 pid_for_children -> pid:[4026533468]
---------------------------------------------------------------------------------------------------
| lrwxrwxrwx    1 root     root             0 Mar 10 14:20 time -> time:[4026531834]              |
| lrwxrwxrwx    1 root     root             0 Mar 10 14:20 time_for_children -> time:[4026531834] |
| lrwxrwxrwx    1 root     root             0 Mar 10 14:20 user -> user:[4026531837]              |
---------------------------------------------------------------------------------------------------
lrwxrwxrwx    1 root     root             0 Mar 10 14:20 uts -> uts:[4026533466]
```
Видим, что id неймспейсов USER и TIME на хосте совпадают с id неймспейсов в контейнере.

Кстати, еще можно было воспользоваться командой lsns:
```shell
lsns
```
```
        NS TYPE   NPROCS     PID USER  COMMAND
------------------------------------------------        
| 4026531834 time        2 1679752 admin -bash | 
------------------------------------------------
4026531835 cgroup      2 1679752 admin -bash
4026531836 pid         2 1679752 admin -bash
-------------------------------------------------  
| 4026531837 user        2 1679752 admin -bash  |
-------------------------------------------------  
4026531838 uts         2 1679752 admin -bash
4026531839 ipc         2 1679752 admin -bash
4026531840 net         2 1679752 admin -bash
4026531841 mnt         2 1679752 admin -bash
```

```shell
docker run debian lsns
```
```
        NS TYPE   NPROCS PID USER COMMAND
------------------------------------------------          
| 4026531834 time        1   1 root lsns       |
| 4026531837 user        1   1 root lsns       |
------------------------------------------------  
4026532582 mnt         1   1 root lsns
4026532583 uts         1   1 root lsns
4026532584 ipc         1   1 root lsns
4026532585 pid         1   1 root lsns
4026532586 cgroup      1   1 root lsns
4026532587 net         1   1 root lsns
```

Пространство TIME появилось в Linux 5.6 и позволяет изолировать системное время. Пространство USER позволяет изолировать пользователей и группы. Позднее в Docker добавили поддержку пространства TIME и сделали его доступным в контейнерах. Кто до этого работал с docker раньше были вынуждены монтировать localtime в контейнеры, чтобы синхронизировать время.
Т.к пространство USER не изолируется, т.е UID хоста шарится с контейнером.
Следовательно root на хосте и root в контейнере один и тот же. Давайте убедимся.

На хосте:
```shell
sudo su 
id
```
```
uid=0(root) gid=0(root) groups=0(root)
```

В контейнере:
```shell
docker run alpine id
```
```
uid=0(root) gid=0(root) groups=0(root),...
```

Но точно ли руты они одинаковы или какой-то root мощнее? Сравним capabilities. Если нет утилиты capsh, то необходимо установить пакет libcap2-bin:

```shell
sudo apt-get install libcap2-bin
```
Итак сравним capabilities.

На хосте:
```shell
capsh --print|grep 'Bounding set'
```
```
Bounding set =cap_chown,cap_dac_override,cap_dac_read_search,cap_fowner,cap_fsetid,cap_kill,cap_setgid,cap_setuid,
cap_setpcap,cap_linux_immutable,cap_net_bind_service,cap_net_broadcast,cap_net_admin,cap_net_raw,cap_ipc_lock,
cap_ipc_owner,cap_sys_module,cap_sys_rawio,cap_sys_chroot,cap_sys_ptrace,cap_sys_pacct,cap_sys_admin,cap_sys_boot,
cap_sys_nice,cap_sys_resource,cap_sys_time,cap_sys_tty_config,cap_mknod,cap_lease,cap_audit_write,cap_audit_control,
cap_setfcap,cap_mac_override,cap_mac_admin,cap_syslog,cap_wake_alarm,cap_block_suspend,cap_audit_read,cap_perfmon,
cap_bpf,cap_checkpoint_restore
```

В контейнере:

```shell
docker run alpine sh -c "apk add -q libcap && capsh --print|grep 'Bounding set'"
```
```
Bounding set =cap_chown,cap_dac_override,cap_fowner,cap_fsetid,cap_kill,cap_setgid,cap_setuid,cap_setpcap,
cap_net_bind_service,cap_net_raw,cap_sys_chroot,cap_mknod,cap_audit_write,cap_setfcap
```
Видим, что root на хосте мощнее, т.е имеет болбше полномочий. Оказывается Docker при запуске контейнера отобрал часть capabilities, чтобы сделать процесс в контейнере более безопасным. В этом примере мы видим, что есть нетривиальные вещи о которых по-хорошему нужно знать.

Давайте еще поговорим об изоляции.
### Пример 2: Видимость процесса в контейнере
Виден ли с хоста процесс, запущенный в контейнере?
К сожалению, на собеседованиях почему-то не всегда отвечают на этот вопрос. 
Что ж давайте посмотрим. Запустим sleep в контейнере:
```shell
docker run -d alpine sleep 1000
```
На хосте:
```shell
ps aux|grep sleep
```
```
root     1688866  0.1  0.0   1624   768 ?        Ss   07:12   0:00 sleep 1000
admin    1688892  0.0  0.0   7076  2176 pts/1    S+   07:12   0:00 grep --color=auto sleep
```
Да, процесс виден и видно, что он запущен под рутом.

Однако из самого контейнера других процессов мы не видим:
```shell
docker run  alpine ps aux
```
```
PID   USER     TIME  COMMAND
    1 root      0:00 ps aux
```

### Пример 3: Форк-бомба
Что будет, если в Linux запустить это:
```shell
:(){ :|:& };:
```
Это не что иное, как форк-бомба (fork-bomb), которая рекурсивно забьет процессы на хосте и выведет его из строя.
<details>
  <summary>
подробнее...
</summary>
<h4>1.	Определение функции</h4>
Запись :() объявляет функцию с именем :.
В Bash можно задавать имена функций практически любыми символами, и здесь выбран именно двоеточие.
Скобки () после имени указывают, что функция не принимает аргументов.
Тело функции заключается в фигурные скобки { ... }.
<h4>2.	Содержимое функции</h4>
Внутри фигурных скобок находится конструкция :|:&.
Первая часть : – это вызов самой функции (рекурсивный вызов).
Символ | – оператор конвейера, который передает вывод левой команды в качестве ввода правой.
Вторая часть : – снова вызов той же функции, получающий данные из конвейера.
Символ & в конце указывает, что этот вызов (то есть вся конструкция) будет выполняться в фоновом режиме.
Таким образом, каждый вызов функции запускает два новых процесса:
	•	Один процесс, который начинает выполнение функции и передает результат через конвейер.
	•	Второй процесс, также запускающий функцию, но параллельно.
<h4>3. Вызов функции</h4>
	После определения функции, в конце команды стоит ;:, что означает вызов функции :.
	Этот единственный внешний вызов инициирует цепочку рекурсивных запусков.
<h4>4.	Последствия выполнения</h4>
•	При каждом вызове функции происходит порождение двух новых вызовов, что приводит к экспоненциальному увеличению количества процессов.
•	Система быстро начинает расходовать все доступные ресурсы (процессорное время, память, таблицу процессов).
•	В итоге, система может стать неотзывчивой или даже зависнуть, требуя перезагрузки.
<h5>5.	Итог</h5>

</details>
Это пример вредоносного кода, который используется для атаки типа “отказ в обслуживании” (DoS) за счет перегрузки системы чрезмерным количеством одновременно запущенных процессов. Так что даже небольшая строка кода может иметь разрушительный эффект на систему, используя рекурсию и параллельное выполнение для быстрого исчерпания ресурсов. Если ее выполнить на хосте - хост упадет, только не проверяйте на проде)

Но что произойдет, если выполнить эту команду в контейнере?
Запустим контейнер:
```shell
docker run -it alpine ash
```
Как мы выяснили ранее, процесс контейнера виден с хоста. Запустим рядом еще один терминал и найдем его pid:
```shell
ps au|grep ash
```
```
admin       1234  0.0  0.0  10820  7200 pts/0    Ss+  21:54   0:00 -bash
admin       2330  0.0  0.0   9192  5632 pts/1    Ss+  21:59   0:00 -bash
admin      63591  0.0  0.0   9192  5632 pts/2    Ss   23:41   0:00 -bash
admin      63619  0.0  0.3 1773624 28160 pts/2   Sl+  23:42   0:00 docker run -it alpine ash
------------------------------------------------------------------------------------------------
| root       63664  0.0  0.0   1736  1024 pts/0    Ss+  23:42   0:00 ash                       |
------------------------------------------------------------------------------------------------
admin      63801  0.0  0.0   9192  5632 pts/3    Ss   23:44   0:00 -bash
admin      63817  0.0  0.0   7076  2176 pts/3    S+   23:44   0:00 grep --color=auto ash
```
У меня это 63664

Посмотрим количество дочерних процессов в контейнере:
```shell
ps --ppid 63664
```
```
    PID TTY          TIME CMD
```
Пока пусто.
Теперь вернемся в первый терминал, где запущен контейнер и запустим нашу форк-бомбу:

```shell
:(){ :|:& };:
```
```
...
can't fork: Resource temporarily unavailable
can't fork: Resource temporarily unavailable
can't fork: Resource temporarily unavailable
can't fork: Resource temporarily unavailable
can't fork: Resource temporarily unavailable
can't fork: Resource temporarily unavailable
can't fork: Resource temporarily unavailable
can't fork: Resource temporarily unavailable
...
```
Бомба отработала, насоздавала нам процессов, но сам хост не упал.
**Сразу**(пока ОС не остановила созданные процессы) перейдем во второй терминал и посмотрим на дочерние процессы в контейнере:

```shell
ps --ppid 63664
```
```
...
 101748 pts/0    00:00:00 ash
 101749 pts/0    00:00:00 ash
 101750 pts/0    00:00:00 ash
 101751 pts/0    00:00:00 ash
 101752 pts/0    00:00:00 ash
 101753 pts/0    00:00:00 ash
 101754 pts/0    00:00:00 ash
 101755 pts/0    00:00:00 ash
 101756 pts/0    00:00:00 ash
 101757 pts/0    00:00:00 ash
 101758 pts/0    00:00:00 ash
 101759 pts/0    00:00:00 ash
 101760 pts/0    00:00:00 ash
 101761 pts/0    00:00:00 ash
 101762 pts/0    00:00:00 ash
...
```

Посчитаем количество дочерних процессов:

```shell
ps --ppid 63664|wc -l
```
```
9439
```

Сам хост не упал и больше процессов не порождается. Таким образом изоляция процесса помогла ограничить область поражения и изолировать fork-бомбу, а следовательно и добавила надежности.

Более того, можно ограничивать лимиты и по cpu, memory:

```shell
docker run --pids-limit=50 --memory=128m --cpus=0.5 alpine
```
Попробуйте испытать изоляцию по ресурсам можно с помощью утилиты stress

Что ж, мы рассмотрели примеры изоляции процессов в контейнерах и увидели, что благодаря изоляции контейнеры могут быть более безопасными, чем процессы на хосте.

Атаки на контейнеры сводятся к тому, чтобы обойти изоляцию и получить доступ к хосту. Защита должна быть многоуровневой, начиная от сборки образа и заканчивая настройкой хоста.



## Побеги из контейнеров
Пример с runc, remapping uid

## Best Practice

Про docker group и полномочия рута

read-only filesystem icc, network, remapping UID\
\
Рассказать про бест практис настройки Docker, в частности про такую вещь как Center Internet Security Benchmark

и утилиты, позволяющие проверить его соблюдение, например:

[https://github.com/docker/docker-bench-security](https://github.com/docker/docker-bench-security)

\\

Не всему в CIS можно верить

Нужно критично относиться к советам по безопасности. К примеру, в CIS советуют включить настройку live restore, однако с ней перестанет работать Swarm. Поэтому если вы используете его в качестве оркестратора - то этот совет вам не подойдет.

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXcVFeo59hQwt2e-BXcKfvdTKjbmcwKVqluALXnDtKTbQDjysuHqTpJYeZZpNiuCKkDfsrijDMl9DdIiaWdqvVkxPC4t62SWeNi4NdzdnOmb0W_Pip5rq4SJloufP1BgrZ513CEINw?key=o4n_CvAEFOAedWxOCWnj3EN0)

## ![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXedkDMk0Cf2YvnyyQ64Ytqe4uH2xTMI3ZdRvYSzETu2WM_fGGwi5pnYZwASNBf0hGXfzE-dhP9-bUlgtj77dE9rLSMTA5JDYDs6BuAURKUrWHLsvAvXEdo1C6_t3hz1qMaHZL-XDw?key=o4n_CvAEFOAedWxOCWnj3EN0)[https://docs.docker.com/config/containers/live-restore/](https://docs.docker.com/config/containers/live-restore/)

## Статика
Ве начинается с FROM
Какой базовый образ выбрать? 
DockerHub: Official Image, Verified Publisher, Sponsored OSS (spponsored by Docker)
Кто-нить ананлизирует базовые образы?
Как собрать base image?
- из файловой системы
- из scratch (часть для гошки, раст,...)
- из iso (rusdacent любит)

### SAST для Dockerfile
## Hadolint

плюсы:
- опирается на рекомендации Docker
- использует ShellCheck для проверки скриптов в RUN (bash скриптов)
- OSS
минусы:
- Haskell

пинить версию - накладные расхды на обновление
Федулаев исключил Hadolint из своего процесса CI/CD, но испольует как линтер для IDE
пример с хадолинтом
тег latest не дает воспроизводимости
инструкция COPY лучше чем ADD, ADD - плохо, но если нужно у ADD есть инстркция checksum
есть хаодлинт онлайн

### Semgrep
- есть отдельная репа с файлами
- есть плейграунд

### Checkov

### Kics (от Checkmarx)

4 уязвимости от сник
снику подсунуть докеримадж с закладкой, подменить команду ls, алиасы
### анализ докер-образов
docker history, dive
scopio

### Dockly
best-practise docker+cis

### Clair +quay
### Trivy
много  таргетов
у триви есть комплаенс - cis benchmark
а что если бекдор закинуть в тест
### Grype+Syft почти равно Trivy

cosign для подписи образов
харбор интегрируеся с косайгном и показывает подписанные образы
косайн+триви умеют делать аттестацию

подписть dct+notaty

Multystaging -->must have!!!

Docker + ИИ? скан
### Registry - чужие и свои


, Trivy, Dockle, Chechov



Cборка образа: DIND, Jib,Kaniko, BuildX, multi stage images

Инструменты: Crane, Trivy, Dockly, Chechov\
\
Jib/Kaniko

Для сокращения поверхности атаки в раннерах, вместо DIND можно использовать утилиты и плагины для сборки контейнеров или в крайнем случае rootless DIND

\\

* Buildx

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXemaf7toNyhH-LjDZok9A6sspyy4-1sWZ9ec6_noXDCPsUzt51IG4p23-0NGZp8agXlLUBZZ3A_YgTD1ll3S0HsqfdgvG4PoFST4B0AE4_IP7uqbwgif-ncaFRUgxVNtMruIm6Scg?key=o4n_CvAEFOAedWxOCWnj3EN0)

В новых версиях buildx - уже используется по умолчанию

Новый инструмент сборки, рассказать про SBOM, метаданные сборки и мультиплатформенность, [provenance attestation](https://docs.docker.com/build/attestations/slsa-provenance/), etc

\\

* Рассказать про distroless образы.\
  \
  Рассказать про Scanning images tools, например Trivy. Рассказать немного про практики, например, про то, что образы надо сканировать не только на этапе CI/CD. Очень важно фоново сканировать репозиторий образов, т.к. могли обнаружить новые уязвимости и важно их пофиксить у себя на проде, в образе, который уже когда-то прошел CI

Для уменьшения вероятности LotL attack (Living off the Land), стоит включать в образы только самый минимум, рассказать про FROM SCRATCH\
\\

Рантайм\
\
Docker compose
--------------

Не используйте docker-compose, написанный на Python, используйте нативную команду\
\\

Ремаппинг UID, пример побега из контейнера без ремаппинга\
Kubernetes vs Swarm

Альтернативные рантаймы:\
Интересные решения просто для расширения майндсета

Gvisor https://gvisor.dev/

## Анализ аномалий

Falco, …

\
\\

### Черновик:

\\

* \\

\\

* Настройка гранулярных прав в Докер

https://docs.docker.com/engine/extend/plugins\_authorization/

\\

* Утилиты для анализа аномалий в рантайме контейнеров, например Falco

\\

* Утилиты для анализа слоев образа

\\

Добавить в книгу полезные ресурсы:

* [Container Security - Liz Rice](https://www.amazon.com/Container-Security-Fundamental-Containerized-Applications/dp/1492056707)
* [Practical Cloud Native Security with Falco](https://www.oreilly.com/library/view/practical-cloud-native/9781098118563/)
* [CIS Benchmark Docker](https://www.cisecurity.org/benchmark/docker)
* [Implementing DevSecOps with Docker and Kubernetes](https://www.amazon.com/Implementing-DevSecOps-Docker-Kubernetes-Experiential/dp/9355511183)

## Квиз по Docker Security

#### 1 Что делает EXPOSE в Dockerfile?

1. только декларирует порты
2. открывает порты
3. нет такой директивы

#### 2 Какая директива в Dockerfile безопаснее?

1. COPY
2. ADD
3. COPY\_ON\_WRITE

#### 3 Какого бита для настройки прав не существует?

1. setgid
2. setuid
3. sticky
4. setguid

#### 4 Виден ли процесс в контейнере со стороны хоста?

1. виден
2. не виден

#### 5 Останется ли пароль в образе?

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXfiutLo3Udtj5dXsugqTO5dyPeoLZJ1RWxf59SFnF-j89D9zxUT54vHjegenRcN2WedzrhOzxrcxbqnH6qMJmOhkGZ1i0RvcMxBSbFjCGy_ybHsASXcjScXz6eUH9zs15jBklur7g?key=o4n_CvAEFOAedWxOCWnj3EN0)

1. да
2. нет

\\

6 Что однозначно идентифицирует образ?

1. URL
2. tag
3. hash
4. это все

\\

7 Под кем запустится процесс в контейнере?

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXenAPe8dC6ZUP2MtYmc0-GrY1e_yah50MIyncjeiUB42rL3lHzCca-ESmQLdqQe_Rj7Xdq_hScAOHpvhJrcn8WvSgY24XRJc6UNsfuaj6-5ia2eJCJi6GU56B3E7zuaQkQXi9eSXw?key=o4n_CvAEFOAedWxOCWnj3EN0)

1. admin
2. root
3. не запустится

#### 8 Руты в контейнере и Рут на хосте? Кто круче?

1. в контейнере круче
2. на хосте круче
3. одинаковое

#### 9 Какое слово сделает рутов равными?

privileged

#### 10 Выполнили следующие команды. Какой код выхода из контейнеров будет?![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXcjdUT2yppLWAEhwMjP7gmiGABJWUJaog8rVJNaudMUlC8CdQp6KGx6it46K2encIMcYLBLd-qCz1QzGuSWAMqzbnzfEcMfwgfDB3e-gU56Qk9cC3s4TeNaNr-ACGHRTQy0xwkVgQ?key=o4n_CvAEFOAedWxOCWnj3EN0)

a) test1=0 test2=0

b) test1=137 test2=137

c) test1=0 test2=137

d) test1=137 test2=0

#### 11 Включили Remapping UID. Какая команда выведет файл secret.txt под пользователем admin?

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXcarNL1T9A4hA9KBxb__ukOqhkuu7dljuL5x9mSjnrC2wSoLIcslGqNCqRDji-FOR8KJiPqqN7gYcOvjjV7DzAxb2LdRhVhwnXyotYVs1kdxf110n5BgiCuNF8nnWrFwY_qSUbw?key=o4n_CvAEFOAedWxOCWnj3EN0)

1. docker run -v $(pwd)/secret.txt:/secret.txt alpine cat /secret.txt
2. docker run --privileged -v $(pwd)/secret.txt:/secret.txt alpine cat /secret.txt
3. docker run --privileged --userns=host -v $(pwd)/secret.txt:/secret.txt alpine cat /secret.txt
4. docker run --privileged --userns=host -v /secret.txt:/secret.txt alpine cat /secret.txt

\\

### Почему не Podman?

\\

Надо ли сканировать образа на вирусы
