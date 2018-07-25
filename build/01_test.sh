#!/bin/bash -x

set -eu

current_directory="$( cd "$(dirname "$0")" ; pwd -P )"

source "${current_directory}/common.sh"

cd "${current_directory}/.."

replace_version_in_pom_with_git_describe

mvn clean test
