#!/bin/bash -x

set -eu

common_directory="$( cd "$(dirname "$0")" ; pwd -P )"

which xml2

replace_version_in_pom_with_git_describe()
{
    git_describe_output="$(git describe)"
    echo "git describe: ${git_describe_output}"

    docker_image_version="$(xml2 < ${common_directory}/../pom.xml | grep '/project/version=' | sed 's/\/project\/version=//')"
    echo "Docker Image Version: ${docker_image_version}"
    sed -i "s/<version>${docker_image_version}<\/version>/<version>${git_describe_output}<\/version>/" ${common_directory}/../pom.xml
}
