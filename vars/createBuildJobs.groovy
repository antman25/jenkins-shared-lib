#!/usr/bin/env groovy

def call() {
    properties([disableConcurrentBuilds()])

    podTemplate {
        node(POD_LABEL) {

            stage('Clone code')
            {
                checkout scm
            }

            stage('Job DSL') {
                def branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
                def config_data = readFile 'config/config.yaml'

                def params = [  'workspace_path' : "${WORKSPACE}",
                                             'branch_name': branch_name,
                                             'config_data'    : config_data]
                jobDsl targets: ['seed_jobs/main.groovy'].join('\n'),
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        lookupStrategy: 'JENKINS_ROOT',
                        additionalParameters: params
            }
        }
    }
}