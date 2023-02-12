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
                def params = [  'root_folder' : 'pipeline_jobs',
                                             'job_testing_folder' : 'job_testing',
                                             'branch_name' : branch_name,
                                             'config_data' : config_data,
                                             'workspace_path' : "${WORKSPACE}"]

                jobDsl targets: ['seed_job/build_seed_jobs.groovy',
                                 'seed_job/build_tenant_root.groovy'].join('\n'),
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        lookupStrategy: 'JENKINS_ROOT',
                        additionalParameters: params
            }
        }
    }
}