#!/bin/bash

# LaCompraGo Environment Setup Script
#
# This script prepares the local environment for building and testing the LaCompraGo Android app.
# It ensures that the required Java version is installed and that the Gradle wrapper is executable.

# --- Configuration ---
REQUIRED_JAVA_VERSION="17"

# --- Functions ---

# Function to print colored messages
print_message() {
    local color=$1
    local message=$2
    case $color in
        "red")    echo -e "\033[0;31m${message}\033[0m" ;;
        "green")  echo -e "\033[0;32m${message}\033[0m" ;;
        "yellow") echo -e "\033[0;33m${message}\033[0m" ;;
        *)        echo "$message" ;;
    esac
}

# --- Main Script ---

# 1. Check for Java
print_message "yellow" "1. Checking for Java..."
if ! command -v java &> /dev/null; then
    print_message "red" "Java is not installed. Please install JDK ${REQUIRED_JAVA_VERSION}."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F. '{print $1}')

if [ "$JAVA_VERSION" -eq "$REQUIRED_JAVA_VERSION" ]; then
    print_message "green" "Java version ${REQUIRED_JAVA_VERSION} is installed."
else
    print_message "red" "Incorrect Java version. Found version ${JAVA_VERSION}, but version ${REQUIRED_JAVA_VERSION} is required."
    print_message "yellow" "Please ensure you have JDK ${REQUIRED_JAVA_VERSION} and that it is the default."
    exit 1
fi

# 2. Set gradlew permissions
print_message "yellow" "\n2. Setting execute permissions for gradlew..."
if [ -f "gradlew" ]; then
    chmod +x gradlew
    print_message "green" "gradlew is now executable."
else
    print_message "red" "gradlew script not found in the root directory."
    exit 1
fi

# 3. Verify setup with Gradle
print_message "yellow" "\n3. Verifying Gradle setup..."
if ./gradlew --version > /dev/null; then
    print_message "green" "Gradle setup verified successfully."
else
    print_message "red" "Gradle verification failed. Please check your setup and network connection."
    exit 1
fi

print_message "green" "\nEnvironment setup complete! You are ready to build the project."
