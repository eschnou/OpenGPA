# OpenGPA - (Open) Agentic is all you need 😁

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

The current version is a minimal POC yet, it already packs a few interesting pieces:
- Works with all LLM supported by [spring-ai](https://spring.io/projects/spring-ai), including running **LLama** locally
- Multi-step task processing with **chain-of-thought** approach
- Action model with easy to extend **actions** for use by the agent
- Upload of **artifacts** to process by the agent
- Download of **artifacts** generated by the agent
- Import documents in knowledge base to support RAG search

## 🚧 Roadmap

Improve the Agentic capabilities:
- ✅ **RAG** enabling the agent to consult vast volume of internal documents
- **Memory** enabling the agent to remember key facts and use them later
- **code generation** and **execution** within the agent (using Groovy scripts?)
- **remote API invocation** to tap into existing enterprise APIs
- **web interactions** allowing the agent to navigate pages submit forms
- **scheduled** jobs for automating workflow
- **triggers** to create complete end-to-end workflows

Make OpenGPA enterprise ready:
- Persistence of tasks/steps
- Proper file storage and management
- User management and access control
- Secure API access through API gateway
- Auditing of task processing costs (token usage)
- Instrumentation and observability

## 🚀 Getting started

### Build and run the server

If you are on a Mac, the following should be enough to get you started and running this
locally. In case of trouble, please reach out on [Discord](https://discord.gg/3XPsmCRNE2). 

> [!WARNING]
> Building requires __Java 21__. If you are on a Mac, you can easily install it
> with `brew install openjdk@21`

By default, opengpa is using OpenAI gpt-4o as its LLM. Check the `application.properties` file 
for configuration options and the spring-ai documentation to configure support for other LLMs.

```bash
mvn clean package -Pproduction
OPENAI_API_KEY=sk-*** java -jar opengpa-server/target/opengpa-server-0.2.0.jar
```

Open the UI on [http://localhost:8000](http://localhost:8000) and login with username `opengpa` and password `opengpa`.


### Debugging

For debugging purposes you can log all interactions and prompts using the following config:
```
opengpa.server.log-prompt=true
opengpa.server.log-folder=/tmp/opengpa/logs
```

# Documentation
- [Setting up an OpengGPA server](documentation/setup.md)
- [Using local open-source LLM](documentation/offline.md)
- [Creating a custom Action](documentation/actions.md)

# Support
- Join us on [Discord](https://discord.gg/3XPsmCRNE2)
- Reach out on [X](https://x.com/opengpa)
- File an [issue](https://github.com/eschnou/OpenGPA/issues) 

# License

MIT License

Copyright (c) 2024 Laurent Eschenauer

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
