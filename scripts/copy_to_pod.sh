#!/bin/bash


kubectl cp -n jenkins ../casc/baseline.yaml jenkins-0:/tmp -c jenkins
