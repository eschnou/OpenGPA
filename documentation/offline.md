# Offline setup

OpenGPA supports open-source local LLMs through the [Ollama](https://www.ollama.com/) integration. This
provides you a 'chatGPT' like experience without having to share any data with external provider. If 
setup on a self-hosted node inside your premise, the AI can be truly 'offline'.

## Requirements

To achieve true 'chatGPT like' quality, you need a very large model such as [Llama3-70b](https://ollama.com/library/llama3) 
or Cohere [command-r-plus](https://ollama.com/library/command-r-plus). These models require massive
resources, putting them out of reach of most indie developers. 

- Running with 5~7b models is possible with a `g5.2xlarge` instance on AWS
- Running very large models required a machine such as `g5.48xlarge`

Setup such an instance is beyond the scope of this tutorial. You will find detailed instructions
at [Ollama](https://ollama.com/download).

## Configuration

Edit the start script in `devops/opengpa.sh` to change the spring profile.

```
java -jar opengpa-server/target/opengpa-server-0.1-SNAPSHOT.jar --spring.profiles.active=ollama
```

If Ollama is running on a different server:

- Edit the OLLAMA_BASE_URL in `/etc/default/opengpa`
- Ensure Ollama is listening for external connections by updating the 
OLLAMA_HOST [configuration](https://github.com/ollama/ollama/blob/main/docs/faq.md#setting-environment-variables-on-linux).