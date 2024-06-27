# Install instructions

> [!WARNING]
> The following is still a very manual and rough setup process.
> This is version 0.1, good for experimentation, not ready for
> production use.

## Requirements

### Instance size
Running OpenGPA against an external LLM provider (e.g. openai) doesn't require much compute. On AWS, a t2.small
will be sufficient. For running a fully off-line setup using a local LLM, view the [offline](offline.md) documentation.

### Operating system
By default, OpenGPA is using [Playwright](https://playwright.dev/) for web browsing. Playwright currently only supports
*Debian* and *Ubuntu* distributions. If you want to install on a RedHat based system, you will have to switch to using
the simple markdown based web crawl.

> [!WARNING]
> Due to a Playwright issue, Ubuntu 24 isn't yet supported. The following was tested on AWS
> with ubuntu 22.04.

## Setup

### Install dependencies

```bash
sudo apt update -y && sudo apt upgrade -y
sudo apt-get install maven openjdk-21-jdk libgbm1 apache2-utils
```

> [!TIP]
> - libgbm1 dependency is only required if you run with the default Playwright browser action.
> - apache2-utils is required to encode user passwords in the configuration file

### Checkout the code and build it

```bash
git clone git@github.com:eschnou/OpenGPA.git
cd OpenGPA
mvn clean package -Pproduction
```

### Configure the systemd service

```bash
 cp devops/opengpa.service /etc/systemd/system/opengpa.service
 cp devops/opengpa.default /etc/default/opengpa
 sudo systemctl enable opengpa.service
```

- Edit `/etc/default/opengpa` to adjust your configuration and insert your OpenAI API Key (or switch to another provider)
- Edit `/etc/systemd/system/opengpa.service` if you have installed the service in another path

### Manage the service

You can use systemd to start/stop the service:

```bash
# Start/stop/restart
sudo systemctl start opengpa.service

# Check the status of the service
sudo systemctl status opengpa.service
```

To view the servie logs, use journalctl:
```bash
journalctl -f --unit=opengpa.service
```

# Configuration

## User configuration

The current version doesn't have a database. Users management is done through a user files. To enable
this option you must uncomment two lines in the `/etc/default/opengpa` configuration.

````
OPENGPA_SERVER_AUTH_PROVIDER=file
OPENGPA_SERVER_AUTH_FILE=/etc/opengpa/users.conf
````

In order to encode a password for a user, you can use htpasswd (from the apache2-utils package).

```bash
htpasswd -bnBC 10 "" secret | tr -d ':\n'
```

## Alternative LLM providers

OpenGPA is built on top of Spring AI. This abstraction layer supports multiple vendors of LLMs
such as Azure, Google, Mistral, Anthropic, etc... We haven't test much these integrations but it shouldn't 
be difficult to switch to another provider.  All configurations properties available in the 
spring-ai [documentation](https://docs.spring.io/spring-ai/reference/1.0/api/chatmodel.html).