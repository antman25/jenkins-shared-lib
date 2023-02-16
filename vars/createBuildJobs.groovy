import org.yaml.snakeyaml.Yaml

def call() {
    properties([disableConcurrentBuilds()])

    podTemplates.pythonTemplate {
        node(POD_LABEL) {
            def params = [:]
            String branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
            String sanitized_branch_name = utils.sanitizeBranchName(branch_name)
            String path_prefix = utils.getPathPrefix(branch_name,"${DELIVERY_BRANCH}")

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

                sh 'cat config/config.yaml'
            }


            stage('Job DSL') {
                try  {
                    def config_data = readFile 'config/config.yaml'
                    def config_yaml = new Yaml().load(config_data)


                    params = [ 'config_yaml' : config_yaml,
                                'branch_name' : sanitized_branch_name,
                                'branch_name_raw' : branch_name,
                                'delivery_branch' : "${DELIVERY_BRANCH}",
                                'job_testing_folder' : "${JOB_TESTING_ROOT}",
                                'workspace_path' : "${WORKSPACE}",
                                'tools_url' : "${TOOLS_URL}" ]
                    //
                    //'dsl/createJenkinsJobDeploy.groovy',
                    //                                     'dsl/jenkins-admin/createJobs.groovy',
                    //'dsl/test-pipelines/createRoot.groovy',
                    //                                     'dsl/test-pipelines/createTestJobs.groovy'
                    jobDsl targets: [
                                     'dsl/job-testing/createRoot.groovy',
                                     'job-automation/dsl/tenants/createTenantJobs.groovy'
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

            stage ('Test All podTemplates')
            {


                pod_template_jobs = [ :]
                pod_template_jobs['podtemplate_python'] = {
                    stage ('podTemplate: python')
                    {
                        build job: "${path_prefix}/test-pipeline/pod-template-python3"
                    }
                }

                pod_template_jobs['podtemplate_docker'] = {
                    stage ('podTemplate: docker')
                    {
                        build job: "${path_prefix}/test-pipeline/pod-template-docker"
                    }
                }

                pod_template_jobs['podtemplate_helm'] = {
                    stage ('podTemplate: helm')
                    {
                        build job: "${path_prefix}/test-pipeline/pod-template-helm"
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
                        build job: "${path_prefix}/test-pipeline/build-docker"
                    }
                }

                build_step_jobs['build_helm'] = {
                    stage ('buildHelmChart')
                    {
                        build job: "${path_prefix}/test-pipeline/build-helm"
                    }
                }

                parallel(build_step_jobs)
            }

        }
    }
}