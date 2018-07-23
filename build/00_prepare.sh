#!/bin/bash -x

set -eu

current_directory="$( cd "$(dirname "$0")" ; pwd -P )"

unshallow_github_clone ()
{
    if [ -f "${current_directory}/../.git/shallow" ]; then
        echo "Git repository is currently a shallow clone. Unshallowing it.."
        ssh-agent bash -c "cd ${current_directory}/..; git fetch --unshallow"
    fi
}

main ()
{
    unshallow_github_clone
}

main
