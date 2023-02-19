import org.yaml.snakeyaml.Yaml

def call() {
    properties([disableConcurrentBuilds()])

    podTemplates.pythonTemplate {
        node(POD_LABEL) {
            def params = [:]
            String sanitized_branch_name = utils.sanitizeBranchName("${BRANCH_NAME}")
            String path_prefix = utils.getPathPrefix(sanitized_branch_name,"${DELIVERY_BRANCH}")

            println("Sanitized branch name: ${sanitized_branch_name}")


            stage('Clone code') {
                checkout scm
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

                sh 'cat config/config.yaml'
            }

            // TODO: Remove when vault is integrated
            withCredentials([usernamePassword(credentialsId: 'bitbucket-plugin-cred', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                stage('Job DSL') {
                    try {
                        def config_data = readFile 'config/config.yaml'
                        def config_yaml = new Yaml().load(config_data)


                        params = ['config_yaml'       : config_yaml,
                                  'branch_name'       : sanitized_branch_name,
                                  'branch_name_raw'   : "${BRANCH_NAME}",
                                  'workspace_path'    : "${WORKSPACE}",
                                  'tools_url'         : "${TOOLS_URL}",
                                   'bootstrap_password' :"${PASSWORD}" ]

                        jobDsl targets: [   'dsl/createTestingRoot.groovy',
                                            'dsl/tenants/createTenantRoot.groovy',

                                            ].join('\n'),
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
            }

            stage ('Test All podTemplates') {
                pod_template_jobs = [ :]
                pod_template_jobs['podtemplate_python'] = {
                    stage ('podTemplate: python')
                    {
                        //build job: "${path_prefix}/test-pipeline/pod-template-python3"
                    }
                }

                pod_template_jobs['podtemplate_docker'] = {
                    stage ('podTemplate: docker')
                    {
                        //build job: "${path_prefix}/test-pipeline/pod-template-docker"
                    }
                }

                pod_template_jobs['podtemplate_helm'] = {
                    stage ('podTemplate: helm')
                    {
                        //build job: "${path_prefix}/test-pipeline/pod-template-helm"
                    }
                }

                parallel (pod_template_jobs)
            }

            stage('Test All build steps')
            {
                build_step_jobs = [:]

                build_step_jobs['build_docker'] = {
                    stage ('buildDockerImage')
                    {
                        //build job: "${path_prefix}/test-pipeline/build-docker"
                    }
                }

                build_step_jobs['build_helm'] = {
                    stage ('buildHelmChart')
                    {
                        //build job: "${path_prefix}/test-pipeline/build-helm"
                    }
                }

                parallel(build_step_jobs)
            }

        }
    }
}