#!/usr/bin/env groovy

def call() {
    properties([disableConcurrentBuilds()])

    node() {

        stage('Clone code')
        {
            checkout scm
        }

        stage('debug')
        {
            sh 'pwd'
            sh 'find /home/jenkins/agent'
        }


        stage ('Job DSL') {
            def config = readFile 'seed_jobs/config.yaml'
            def params = ['workspace': "${WORKSPACE}",
                                       'config' : config ]
            jobDsl targets: ['seed_jobs/main.groovy'].join('\n'),
                    removedJobAction: 'DELETE',
                    removedViewAction: 'DELETE',
                    lookupStrategy: 'JENKINS_ROOT',
                    additionalParameters: params
        }
    }
}