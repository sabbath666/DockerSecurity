# Docker Security

Контейнеризация уверенно заняла свое место в мире DevOps. Однако безопасность Docker-контейнеров остается актуальной проблемой. И далеко не каждый инженер знает как правильно и безопасно готовить, поставлять, развертывать и следить за контейнерами в рантайме. Сейчас копнем чуть глубже и разберемся как харденить Docker, но сначала вот вопрос: рут в контейнере и рут на хосте - это тот же рут или нет? А чем отличается и отличается ли? Если ты задумался, то тебе сюда.

<добавить агенду>

## Изоляция

Контейнеризация - это не виртуализация. Контейнер - это просто обычный процесс, который запускается в изолированной среде, но это не какая-то отдельная изолированная среда. Процесс в контейнере - это процесс на хосте, который изолирован с помощью стандартных механизмов в Linux: cgroups2, namespaces и системный вызов [pivot root](https://tbhaxor.com/pivot-root-vs-chroot-for-containers/) (который пришел на смену chroot, как более надежный).\\

Что ж начнем с изоляции и рассмотрим несколько поучительных примеров.

### Пример 1: Root на хосте и root в контейнере: кто мощнее?

Сравним неймспейсы в хосте и на контейнере:

<figure><img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXdax7Z_5TGBCwHYubUVpY8_WbRenTixR7EdtviJZq9CghIRsTlNiRCEyPr1kUTGxIsVmUEyJ6ByCn_yMjbwcIQi55M79Bgig28cn2HoeQQQOn3AtAOx2Dp4Vseyi7bVFtB06Hc4Dg?key=o4n_CvAEFOAedWxOCWnj3EN0" alt=""><figcaption></figcaption></figure>

Очень наглядно видно, что почти все id неймспейсов различаются, а USER - нет, т.к пространство UID хоста шарится с контейнером => root на хосте и root в контейнере один и тот же. Давайте убедимся:

<figure><img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXf_gV-CDu8kNODAodOhedb1IPVFOOq1rMemGELUYR7xyuPofGzwOygm5NGbrx_XgI49_cI2PyE89lkJXrSGnFZe_4VDpNSdfmGX_ZAnezdo-xgIqHFjEdSr3Mg-ueV8JrGf0ryY?key=o4n_CvAEFOAedWxOCWnj3EN0" alt=""><figcaption></figcaption></figure>

Но точно ли они одинаковы или какой-то root мощнее? Сравним capabilities. Если нет утилиты capsh, то необходимо установить пакет libcap2-bin:

<figure><img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXfw26UsX1ml0QyaXtqc4bAQlUmxeURGAp4AO5ct1aOK9xRjRqnsWktQJMo1PfXWajARgYsA1DfImc-zaRCTsd2Z2aEgM6EubnJk3-unQ57tK-081nm77sIkFn1SG5Ki2BxtLWse?key=o4n_CvAEFOAedWxOCWnj3EN0" alt=""><figcaption></figcaption></figure>

Итак сравним capabilities.

На хосте:

<figure><img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXe8ftRKuShdPgdTK79wfnz4HwLvs9996qdRQ1V9qJOt0laIb6b2yNFmT3NBG1P88TG2CPaZHhA46IRgb0oymzRJwnE4tYwLpRI5AEdCPM6vI5DPU_34zZ6pQVMugNUSN5MVCKYlCQ?key=o4n_CvAEFOAedWxOCWnj3EN0" alt=""><figcaption></figcaption></figure>

В контейнере:

<figure><img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXfhxXyUeKi1hV_wQ3Zq6q5c9YasACbd47VBnPhU0xvz7tX44bL1AQHT1E68gV3FmEvAN6arXmCd1orHvNYoOSDWX4Sl5YOTG7t0v-s90vHcDxoW2hMxULKO0NS4HqvznFkiM2t9AQ?key=o4n_CvAEFOAedWxOCWnj3EN0" alt=""><figcaption></figcaption></figure>

Видим, что root на хосте мощнее. Оказывается Docker при запуске контейнера отобрал часть Capabilities, чтобы сделать процесс в контейнере более безопасным. В этом примере мы видим, что есть нетривиальные вещи о которых по-хорошему нужно знать.

\
TODO Пример с видимостью процесса в контейнере с хоста\\

<figure><img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXet7NVtpZLmmuohuy_Acq32Sr-3UobvxhGm3a8ZhD-VecFt8Daiwo57krZoywHHIxdla5QF5VOvAWje_BaCUNXMPpD3Q850jE60HQnwUWBXwyWkADKgzri3j5uLoSyvHud4ccVvgA?key=o4n_CvAEFOAedWxOCWnj3EN0" alt=""><figcaption></figcaption></figure>

<figure><img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXdQHoraZD6ush9Cs9uz0FHv2UUcXmpHUdfT1jOYSkKnplvU2yMEbBmsomdwOE5hFYnjm8A-U6-IaCr_QIH0guBhlXDBFiUvkRzyDjCTRahOuee0IuTYtPYUNJDQMtoQdG0nVTomDg?key=o4n_CvAEFOAedWxOCWnj3EN0" alt=""><figcaption></figcaption></figure>

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

Какой базовый образ выбрать?

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
