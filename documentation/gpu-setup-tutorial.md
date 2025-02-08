# Setting up GPU-Enabled EC2 for ML Development

This guide walks through setting up an AWS EC2 instance with GPU support for machine learning development, 
specifically focused on running containerized ML workloads and Ollama.

## Prerequisites

- AWS Account with appropriate permissions
- Basic familiarity with AWS EC2
- SSH client installed on your local machine

## Step 1: Launch EC2 Instance

### Instance Selection
1. Navigate to EC2 Dashboard in AWS Console
2. Click "Launch Instance"
3. Choose Ubuntu Server 22.04 LTS AMI
4. Select g5.2xlarge (NVIDIA A10G) for instance type

### Network Configuration
1. Create or select a VPC
2. Configure Security Group:
   ```
   SSH (22): Your IP
   Custom TCP (11434): Your IP (for Ollama)
   Custom TCP (3000): Your IP (for Opengpa backend)
   ```
3. Create or select a key pair for SSH access

## Step 2: Connect to Instance

```bash
ssh -i /path/to/key.pem ubuntu@your-instance-ip
```

## Step 3: System Setup

### Update System
```bash
sudo apt-get update && sudo apt-get upgrade -y
sudo apt-get install -y linux-headers-$(uname -r)
```

### Install NVIDIA Drivers
```bash
sudo apt-get install -y nvidia-driver-535
sudo reboot
```

After reboot, reconnect and verify GPU:
```bash
nvidia-smi
```

## Step 4: Docker Installation

If you want to run OpenGPA on the same machine, you will need docker setup.

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker
```

## Step 5: NVIDIA Container Toolkit

```bash
# Add NVIDIA package repository
curl -fsSL https://nvidia.github.io/libnvidia-container/stable/deb/nvidia-container-toolkit.list | \
  sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list

# Add NVIDIA GPG key
curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | \
  sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg

# Update package list
sudo apt-get update

# Install NVIDIA Container Toolkit
sudo apt-get install -y nvidia-container-toolkit

# Configure Docker runtime
sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
```

## Step 6: Verify GPU Container Support

```bash
# Test with NVIDIA CUDA container
docker run --rm --runtime=nvidia --gpus all \
  nvidia/cuda:11.6.2-base-ubuntu20.04 nvidia-smi
```

You should see GPU information displayed, confirming proper setup.

## Step 7: Install Ollama

```bash
# Install Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Configure Ollama for GPU and external access
sudo mkdir -p /etc/systemd/system/ollama.service.d/
sudo tee /etc/systemd/system/ollama.service.d/override.conf << EOF
[Service]
Environment="OLLAMA_HOST=0.0.0.0"
Environment="NVIDIA_VISIBLE_DEVICES=all"
EOF

# Restart Ollama service
sudo systemctl daemon-reload
sudo systemctl restart ollama
```

## Testing Ollama

```bash
# Pull a model
ollama pull llama2

# Test inference
ollama run llama2 "Tell me a joke"
```

## Monitoring and Maintenance

### Monitor GPU Usage
```bash
nvidia-smi -l 1  # Updates every second
```

### Check Ollama Status
```bash
sudo systemctl status ollama
```

### View Ollama Logs
```bash
sudo journalctl -u ollama -f
```

## Troubleshooting

### Common Issues and Solutions

1. **GPU Not Detected**
   ```bash
   sudo ubuntu-drivers autoinstall
   sudo reboot
   ```

2. **Docker Permission Issues**
   ```bash
   sudo chmod 666 /var/run/docker.sock
   ```

3. **Ollama Connection Issues**
   - Check security group settings
   - Verify OLLAMA_HOST setting
   - Check firewall rules: `sudo ufw status`

4. **Out of Memory**
   - Monitor memory: `free -h`
   - Check GPU memory: `nvidia-smi`
   - Consider using a larger instance type

## Additional Resources

- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)
- [NVIDIA Container Toolkit Documentation](https://docs.nvidia.com/datacenter/cloud-native/)
- [Ollama Documentation](https://ollama.com/docs)
- [Docker Documentation](https://docs.docker.com/)
