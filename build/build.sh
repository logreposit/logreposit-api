#!/bin/bash -x

current_directory="$( cd "$(dirname "$0")" ; pwd -P )"

${current_directory}/01_test.sh && ${current_directory}/02_build.sh
