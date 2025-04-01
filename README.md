# OpenGPA - Self-hosted General Purpose Agent

[![Twitter Follow](https://img.shields.io/twitter/follow/opengpa?style=social)](https://twitter.com/opengpa) &ensp;
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**OpenGPA** is an Open-source General Purpose Agent. A self-hosted solution supporting smart AI agent developments
with chain of thought, tool use and memory access through RAG.

- Agentic system with multi-step-reasoning, chain-of-thought, ReAct, tool use etc
- Support for [all major LLMs](https://docs.spring.io/spring-ai/reference/api/chatmodel.html) such as LLama, Mistral, Anthropic, OpenAI, etc.
- Off-line first. You can run on your own box with a local LLM. Data won't leave the server. All the power of GPT without the data privacy nightmare.
- Extensible framework allows to plug your own actions, such as calling into internal services and APIs
- Simple UI exposing insight on the GPA internal reasoning and actions
- Built-in RAG to search internal document stores
- Free and open source, deploy anywhere, customize to your needs

<p align="center">
  <img src="/assets/opengpa_ui.png" width="640" />
</p>

## :pencil2: Design principles

OpenGPA is much more than a UI on top of a LLM. It implements an agentic workflow, where a LLM is used as the 
*brain* of an agent to reason through multiple steps of planning, reasoning, and tool use.  In particular, OpenGpa 
is using the [ReAct](https://arxiv.org/abs/2210.03629) approach to verbally reason on the next step, decide on the action to execute, 
and observe the outcome.

```
{
  "reasoning": "The user wants to know the current weather in Liege, Belgium. 
                The best action to get this information is to perform a web search with 
                the query 'current weather in Liege, Belgium'. The result of this action 
                will then be used to respond to the user's request. 
                This is not the final action as we have to get the results from the web 
                search first."
  "action": {
    "name": "webSearch",
    "arguments": {
      "query": "current weather in Liege, Belgium"
    }
  },
  "is_final": false
}
```

Action selection is based on a catalog of action that can easily be extended through code. You could add an action
to tap into an internal service to fetch some data, or a workflow engine to trigger a next step, etc.

## 🛠️️ Key Features

The current version already packs a few interesting pieces:
- Works with all LLM supported by [spring-ai](https://spring.io/projects/spring-ai), including running **LLama** locally
- Multi-step task processing with **chain-of-thought** approach
- Action model with easy to extend **actions** for use by the agent
- Upload of **artifacts** to process by the agent
- Download of **artifacts** generated by the agent
- Import documents in knowledge base to support **Retrieval Augmented Generation** search
- Web browsing and web search
- Model Context Protocol [MCP](https://modelcontextprotocol.io/introduction) tap into existing enterprise APIs

## 🚧 Roadmap

Improve the Agentic capabilities:
- **Memory** enabling the agent to remember key facts and use them later
- **code generation** and **execution** within the agent (using Groovy scripts?)
- **scheduled** jobs for automating workflow
- **triggers** to create complete end-to-end workflows

Make OpenGPA enterprise ready:
- Persistence of tasks/steps
- Auditing of task processing costs (token usage)
- Instrumentation and observability

# 🚀 Getting started

In case of trouble, please reach out on [Discord](https://discord.gg/3XPsmCRNE2) for help!

## Quickstart (no need to clone this repo)

The `docker-compose.quickstart.yml` makes it easy to launch the latest stable version with 
a postgresql database (with pg_vector extension), the opengpa backend and the frontend, on the same
host.

```
curl -O https://raw.githubusercontent.com/eschnou/OpenGPA/main/docker-compose.quickstart.yml
echo "OPENAI_API_KEY=your-key-here" > .env
docker compose -f docker-compose.quickstart.yml up -d
```

## Build and run with Docker

The simplest way to build and launch OpenGPA is using the provided Docker compose file:

```bash
export OPENAI_API_KEY=sk-*** 
docker compose up --build
```

This will build the opengpa image from source with all required dependencies (in particular the Playwright
dependencies for web browsing) and launch the service on port 3000. Opening http://localhost:3000 will lead you
to the OpenAPI documentation.

## Build and run the server

If you are on a Mac, the following should be enough to get you started and running this
locally.  

> [!WARNING]
> Building requires __Java 21__. If you are on a Mac, you can easily install it
> with `brew install openjdk@21`

By default, opengpa is using OpenAI gpt-4o as its LLM. Check the `application.properties` file 
for configuration options and the spring-ai documentation to configure support for other LLMs.


```bash
mvn clean package -Pproduction
docker compose up -d db
OPENAI_API_KEY=sk-*** java -jar opengpa-server/target/opengpa-server-0.2.0.jar
```

The `docker compose up -d db` lauches the database part from the docker compose as opengpa requires
a postgres database with pg_vector for the RAG feature. 

# 🖥️ User Interface

The User Interface is available in the [OpenGPA Frontend repository](https://github.com/eschnou/opengpa-frontend) and
can be launched with docker.

```
git clone https://github.com/eschnou/opengpa-frontend
cd opengpa-frontend
docker build -t opengpa-frontend:latest .
docker run -p 8000:8000 opengpa-frontend
```

# 🐛 Debugging

For debugging purposes you can log all interactions and prompts using the following config:
```
opengpa.server.log-prompt=true
opengpa.server.log-folder=/tmp/opengpa/logs
```

# 🤖 REST API

You can interact with your agent programatically through the [REST API](documentation/api.md). The up-to-date API Documentation
is available on the backend server at [http://localhost:3000/swagger-ui.html](http://localhost:3000/swagger-ui.html)
with Swagger. You can also download the [yml](http://localhost:3000/api-docs.yml) or [json](http://localhost:3000/api-docs)
version of the API Documentation.

# Using MCP with OpenGPA

OpenGPA can leverage [MCP](https://modelcontextprotocol.io/introduction) to connect to third party services and use
their tools. You can look at the maintained list of servers on the [official repository](https://github.com/modelcontextprotocol/servers) or
on marketplaces like [mcp.so](https://mcp.so/).

MCP cannot be used with the starter Docker image. You need to rebuild your own docker image or run the java server
directly from CLI. In order to use MCP, you should:

1. Configure the path to your MCP server configuration in application.properties
```
spring.ai.mcp.client.stdio.servers-configuration=/path/to/mcp-servers.json
```

2. Configure your mcp-servers.json
```
{
  "mcpServers": {
      "time": {
        "command": "docker",
        "args": ["run", "-i", "--rm", "mcp/time"]
      }
  }
}
```

# 📚 Using OpenGPA Libraries

OpenGPA is designed as a modular system with reusable libraries. You can integrate these libraries into your own Java applications.

## Maven Repository

Our libraries are hosted at `https://dist.opengpa.org/packages`. To use them, add the repository to your pom.xml:

```xml
<repositories>
    <repository>
        <id>opengpa-repository</id>
        <name>OpenGPA Repository</name>
        <url>https://dist.opengpa.org/packages</url>
    </repository>
</repositories>
```

## Available Modules

### Core Module

The core module provides the foundational components for building agentic applications:

```xml
<dependency>
    <groupId>org.opengpa</groupId>
    <artifactId>opengpa-core</artifactId>
    <version>0.4.0</version>
</dependency>
```

### Actions Module

For integrating with the extended action catalog:

```xml
<dependency>
    <groupId>org.opengpa</groupId>
    <artifactId>opengpa-actions</artifactId>
    <version>0.4.0</version>
</dependency>
```

### RAG Module

For retrieval-augmented generation capabilities:

```xml
<dependency>
    <groupId>org.opengpa</groupId>
    <artifactId>opengpa-rag</artifactId>
    <version>0.4.0</version>
</dependency>
```

### MCP Module

For Model Context Protocol integration:

```xml
<dependency>
    <groupId>org.opengpa</groupId>
    <artifactId>opengpa-mcp</artifactId>
    <version>0.4.0</version>
</dependency>
```

# Documentation
- [Using local open-source LLM](documentation/offline.md)
- [Creating a custom Action](documentation/actions.md)

# Support
- Join us on [Discord](https://discord.gg/3XPsmCRNE2)
- Reach out on [X](https://x.com/opengpa)
- File an [issue](https://github.com/eschnou/OpenGPA/issues) 

# License

MIT License

Copyright (c) 2024-2025 Laurent Eschenauer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
