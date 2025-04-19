FROM ubuntu:24.04

# Avoid interactive prompts during installation
ENV DEBIAN_FRONTEND=noninteractive

# Install essential tools and dependencies
RUN apt-get update && apt-get install -y \
    python3.12 \
    python3-pip \
    python3-venv \
    openjdk-11-jdk \
    maven \
    wget \
    unzip \
    git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Verify installed versions
RUN java -version && \
    mvn -version && \
    python3 --version

# Create workspace directory
WORKDIR /havoc-mutation-eval

# Copy the project files
COPY . /havoc-mutation-eval/


# Set up Python virtual environment and install requirements
RUN python3 -m venv /havoc-mutation-eval/.venv && \
    . /havoc-mutation-eval/.venv/bin/activate && \
    pip3 install --no-cache-dir -r requirements.txt

# Build the fuzzers
RUN cd /havoc-mutation-eval/fuzzers && ./setup.sh

# Create a directory for output data
RUN mkdir -p /havoc-mutation-eval/data/raw/fresh-baked

# Set the entry point script
COPY docker-entrypoint.sh /havoc-mutation-eval/
RUN chmod +x /havoc-mutation-eval/docker-entrypoint.sh

ENTRYPOINT ["/havoc-mutation-eval/docker-entrypoint.sh"]
