
![Deprecated](https://img.shields.io/badge/status-deprecated-red)
# Rodan Telecom Signaling Exploitation Framework

### Disclaimer
Rodan is provided for security testing and educational use on systems you own or are authorised to assess. Any misuse is your sole responsibility. Illegal use may lead to criminal charges. The authors and Etisalat Egypt accept no liability for actions performed with this framework.


> **⚠️** This project is no longer actively maintained. Active development continues at [AymanElSherif/Rodan][fork_url].

## Authors
* [Ayman ElSherif][ayman]

## 1. Introduction
Rodan is a telecom signaling exploitation framework created and maintained by 
[Etisalat Egypt][et] Research Labs (E-Labs). This framework includes a suite of 
modules that enable users to exploit vulnerabilities in the signaling protocols 
used by mobile operators.<br/><br/>
Rodan currently supports SS7 and Diameter protocols with plans to support GTP and SIP.

## 2. Features

* Network and Subscriber Information Disclosure.
* Location Tracking.
* Call and SMS Interception.
* Fraud.
* GT/Host Brute Forcing.
* Advanced Filtering Bypass:
  * Malformed ACN.
  * Global OpCodes.
  * Double MAP Components.
  * E.214 Numbering Plan
  * Calling Party/Origin Host Spoofing.
* SS7 Simulation Network.

Rodan has a look and feel similar to the Metasploit Framework, reducing the learning curve for new users.

> **Note:** Knowledge of SS7 and Diameter signaling protocols is required to use Rodan.

## 3. Usage
Rodan can be used either via prebuilt Docker images (recommended) or by building it manually from the source code.

### 3.1 Docker Images
The easiest way to use Rodan is through prebuilt Docker images. These include the Rodan Intruder module and an SS7 simulation network to test selected SS7 attacks.

#### 3.1.1. Prerequisites:
* [Docker Runtime][docker]
* [Docker Compose][docker-compose]
* [Manage Docker as a non-root User][docker-non-root]
* Download [docker-compose.ymle][docker-compose-yml] file to current directory

#### 3.1.2. Start docker images:
Run below command from the directory containing the downloaded docker-compose.yml file.

```bash
$ docker-compose up
```

Attach to the Rodan Intruder container, then run the help command inside the Intruder.<br/>
*(Note: `rlwrap` is used to provide a smoother and more convenient CLI experience.)*<br/>
```bash
$ docker exec -ti intruder rlwrap java -jar /app/app.jar
```

### 3.2 Build from source:
If you prefer, Rodan can be built manually from the source code.
###### Supported OS:
* Linux OS

#### 3.2.1. Download source code
##### Download and extract the source:
[Download from GitHub][master]
```bash
$ unzip rodan-master
$ cd rodan-master
```
> **Note:** You can also clone the repository directly: `git clone https://github.com/Etisalat-Egypt/Rodan.git`

#### 3.2.2. Build
To create the latest development build:
##### Install Build Tools
* [JDK 17 64-bit][jdk17]
* [Maven 3][maven]

##### Install Linux SCTP Tools
```bash
$ sudo apt install lksctp-tools
```

##### Build Framework and Stack Extensions 
```bash
$ cd build/scripts
$ chmod +x build-standalone.sh
$ ./build-standalone.sh
```

### 3.2.3. Run
1. Edit SS7/Diameter association details, and IP addresses in YAML configuration files included in build/config folder.
2. Start Rodan:
```bash
$ cd build/scripts
$ chmod +x start-standalone.sh
$ ./start-standalone.sh
```


[et]: https://www.eand.com.eg/
[jdk17]: https://openjdk.java.net/projects/jdk7/
[maven]: https://maven.apache.org/download.cgi
[master]: https://codeload.github.com/Etisalat-Egypt/Rodan/zip/refs/heads/main
[docker]: https://docs.docker.com/engine/install/
[docker-compose]: https://docs.docker.com/compose/install/
[docker-non-root]: https://docs.docker.com/engine/install/linux-postinstall/#manage-docker-as-a-non-root-user
[docker-compose-yml]:https://github.com/Etisalat-Egypt/Rodan/blob/main/build/docker/docker-compose.yml
[fork_url]:https://github.com/AymanElSherif/Rodan
[ayman]: https://github.com/AymanElSherif
