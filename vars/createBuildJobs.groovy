import org.yaml.snakeyaml.Yaml

def call() {
    properties([disableConcurrentBuilds()])

    podTemplates.pythonTemplate {
        node(POD_LABEL) {
            def params = [:]
            String branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
            String sanitized_branch_name = utils.sanitizeBranchName(branch_name)
            println("Sanitized branch name: ${sanitized_branch_name}")


            stage('Clone code') {
                checkout scm
                /*checkout([$class: 'GitSCM', branches: [[name: branch_name]],
                                            extensions: [],
                                            userRemoteConfigs: [[url: 'https://github.com/antman25/jenkins-shared-lib.git']]])*/
            }

            stage('Build Config')
            {
                println("Executing command in python3 container")
                container('python')
                {
                    sh '''
                        python3 -m pip install PyYAML && \
                        python3 config/build_config.py                         
                       '''
                }

                sh 'cat config/config_test.yaml'
            }


            stage('Job DSL') {
                try  {
                    def config_data = readFile 'config/config.yaml'
                    def config_yaml = new Yaml().load(config_data)


                    params = [ 'config_yaml' : config_yaml,
                                'branch_name_raw' : branch_name,
                                'branch_name' : sanitized_branch_name,
                                'delivery_branch' : "${DELIVERY_BRANCH}",
                                'pipeline_root_folder' : "${PIPELINE_ROOT}",
                                'job_testing_folder' : "${JOB_TESTING_ROOT}",
                                'workspace_path' : "${WORKSPACE}",
                                'tools_url' : "${TOOLS_URL}",
                                'smoketest_podtemplate_python' : 'template-test-python']

                    jobDsl targets: ['job-automation/dsl/pipeline/create_pipeline_jobs.groovy',
                                     'job-automation/dsl/tenants/create_tenant_jobs.groovy',
                                     'job-automation/dsl/smoketest/create_smoketest_jobs.groovy' ].join('\n'),
                            removedJobAction: 'DELETE',
                            removedViewAction: 'DELETE',
                            lookupStrategy: 'JENKINS_ROOT',
                            additionalParameters: params
                }
                catch (Exception ex) {
                    println("Exception: ${ex.toString()}")
                    sh('exit 1')
                }
            }

            stage ('Smoketest podTemplates')
            {
                String path_prefix = utils.getPathPrefix(branch_name,"${DELIVERY_BRANCH}")

                parallel_jobs = [ :]
                parallel_jobs['podtemplate_python'] = {
                    stage ('podTemplate: python')
                    {
                        build_path ="${path_prefix}/${PIPELINE_ROOT}/${SMOKETEST_ROOT}/template-test-python"
                        println("Adding job to paralle build: ${build_path}")
                        build job: build_path
                    }
                }

                parallel (parallel_jobs)
            }
        }
    }
}