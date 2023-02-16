#!/bin/bash
echo "Removing old mirror folder"
rm -rf mirror
echo "Mirroring repo"
git clone --mirror https://github.com/antman25/jenkins-shared-lib.git mirror && \
cd mirror && \
git remote set-url origin ssh://git@bitbucket.antlinux.local:7999/pip/jenkins-shared-lib.git && \
git push --mirror