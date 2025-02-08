# Offline setup

OpenGPA supports open-source local LLMs through the [Ollama](https://www.ollama.com/) integration. This
provides you a 'chatGPT' like experience without having to share any data with external provider. If 
setup on a self-hosted node inside your premise, the AI can be truly 'offline'.

## Requirements

To achieve true 'chatGPT like' quality, you need a very large model such as [Llama3.3-70b](https://ollama.com/library/llama3.3) 
or [Deepseek V3 650b](https://ollama.com/library/deepseek-v3). These models require massive
resources, putting them out of reach of most indie developers. 

However, thanks to newer instruction following models such as [Qwen 2.5](https://ollama.com/library/qwen2.5), it is possible
to achieve a decent experience on a much smaller machine. The following are instructions to configure OpenGPA on AWS
with Qwen 2.5 14b. 

We are going to use a `g5.2xlarge` instance, which means a single H10 GPU with 24GB of memory. Comparable performance
should be achievable on a desktop with a high-end GPU card.

## Configure the AWS Instance to run Ollama

Refer to [this tutorial](gpu-setup-tutorial.md) for a step-by-step setup of Ollama on an AWS GPU machine.

## Pull the required models

We are going to use `qwen2.5:14b` as our reasoning model and `nomic-embed-text` as a lightweight embedding model for
the knowledge management RAG feature. First step is to pull both models:

```
ollama pull qwen2.5:14b
ollama pull nomic-embed-text
```

## Extend qwen context window

By default in Ollama, context windows are very small. We need however a much larger context window to support the
chain-of-thought and action prompts in OpenGPA. We are going to achieve this by creating our own model through
a Ollama configuration.

Create a file named `Modelfile` with the following content:

```
# Modelfile
FROM qwen2.5:14b
PARAMETER num_ctx 65536
```

Create the new ollama model:

```
ollama create -f Modelfile qwen2.5:14b_lg
ollama list
```

In your model list, you should now see a new model named qwen2.5:14b_lg.

## Launch OpenGPA using the new model

If you haven't done it already, check out the ollama backend code:

```
git clone https://github.com/eschnou/OpenGPA.git
cd OpenGPA
```

Build and launch the backend with Ollama support and the newly created model. Since we are running OpenGPA in a docker
container, we need to configure the OLLAMA_BASE_URL to connect to the host server and not the docker machine. On Linux
this is done with the special 172.17.0.1 IP address that refers to the host environment.

```
SPRING_PROFILES_ACTIVE=ollama OLLAMA_MODEL=qwen2.5:14b_ctx OLLAMA_BASE_URL=http://172.17.0.1:11434 docker compose up --build -d
```
