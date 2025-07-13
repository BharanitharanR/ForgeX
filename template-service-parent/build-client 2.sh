#!/bin/bash

# Build the template service client
cd template-service-client
mvn clean install -DskipTests

# Return to the root directory
cd .. 