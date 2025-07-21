FROM ubuntu:20.04

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator

# Cài Java, wget, unzip
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk wget unzip curl git && \
    mkdir -p $ANDROID_HOME/cmdline-tools

# Tải và giải nén Android Command-line Tools
RUN wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip && \
    unzip cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools && \
    mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest && \
    rm cmdline-tools.zip

# Cài platform-tools, build-tools, platforms, v.v.
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34"

WORKDIR /workspace
