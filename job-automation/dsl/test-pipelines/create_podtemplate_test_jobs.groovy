import groovy.transform.Field
@Field final String VALIDIATION_PATH = 'validation'

String getPathPrefix(boolean is_delivery_branch)
{
    if (is_delivery_branch == true)
    {
        return ""
    }
    else
    {
        return "/${job_testing_folder}/${branch_name}"
    }
}

boolean createSmoktestRoot(String path_prefix)
{
    try {
        def smoketest_root ="${path_prefix}/${pipeline_root_folder}/${SMOKETEST_ROOT}"
        folder(smoketest_root)
        {
            displayName ('020 - Smoketests')
            description("Job location of shared-lib smoke tests")
        }

        def create_smoktest_python = createSmoktestTemplatePython(smoketest_root)
        if (create_smoktest_python) {
            println("createSmoktestRoot() Create smoketest podTemplate python: SUCCESS")
        }
        else {
            println("createSmoktestRoot() Create smoketest podTemplate python: FAILURE")
            return false
        }

        def create_smoketest_docker = createSmoktestTemplateDocker(smoketest_root)
        if (create_smoketest_docker) {
            println("createSmoktestRoot() Create smoketest podTemplate docker: SUCCESS")
        }
        else {
            println("createSmoktestRoot() Create smoketest podTemplate docker: FAILURE")
            return false
        }

        def create_smoketest_helm = createSmoktestTemplateHelm(smoketest_root)
        if (create_smoketest_helm) {
            println("createSmoktestRoot() Create smoketest podTemplate helm: SUCCESS")
        }
        else {
            println("createSmoktestRoot() Create smoketest podTemplate helm: FAILURE")
            return false
        }

        def create_smoketest_build_docker = createSmoktestBuildDocker(smoketest_root)
        if (create_smoketest_build_docker) {
            println("createSmoktestRoot() Create smoketest buildDockerImage: SUCCESS")
        }
        else {
            println("createSmoktestRoot() Create smoketest buildDockerImage: FAILURE")
            return false
        }


    }
    catch (Exception ex) {
        println("createSmoktestRoot() Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean createSmoktestTemplatePython(String path_prefix)
{
    try {
        pipelineJob("${path_prefix}/template-python") {
            displayName("podTemplate python")
            description("Exercise python podTemplate")
            logRotator {
                numToKeep(20)
            }
            environmentVariables {
                env('BRANCH_NAME', branch_name)
            }

            definition {
                cpsScm {
                    scm {
                        git {
                            remote { url(tools_url) }
                            branches(branch_name)
                            scriptPath('smoketests/template_tests/python3/Jenkinsfile')
                            extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                        }
                    }
                }
            }
        }


    }
    catch (Exception ex) {
        println("createSmoktestTemplatePython() Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean createSmoktestTemplateDocker(String path_prefix)
{
    try {
        pipelineJob("${path_prefix}/template-docker") {
            displayName("podTemplate docker")
            description("Exercise docker podTemplate")
            logRotator {
                numToKeep(20)
            }
            environmentVariables {
                env('BRANCH_NAME', branch_name)
            }

            definition {
                cpsScm {
                    scm {
                        git {
                            remote { url(tools_url) }
                            branches(branch_name)
                            scriptPath('smoketests/template_tests/docker/Jenkinsfile')
                            extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                        }
                    }
                }
            }
        }


    }
    catch (Exception ex) {
        println("createSmoktestTemplatePython() Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean createSmoktestTemplateHelm(String path_prefix)
{
    try {
        pipelineJob("${path_prefix}/template-helm") {
            displayName("podTemplate helm")
            description("Exercise helm podTemplate")
            logRotator {
                numToKeep(20)
            }
            environmentVariables {
                env('BRANCH_NAME', branch_name)
            }

            definition {
                cpsScm {
                    scm {
                        git {
                            remote { url(tools_url) }
                            branches(branch_name)
                            scriptPath('smoketests/template_tests/helm/Jenkinsfile')
                            extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                        }
                    }
                }
            }
        }


    }
    catch (Exception ex) {
        println("createSmoktestTemplateHelm() Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean createSmoktestBuildDocker(String path_prefix)
{
    try {
        pipelineJob("${path_prefix}/build-docker") {
            displayName("buildDockerImage")
            description("Build Docker Image Test")
            logRotator {
                numToKeep(20)
            }
            environmentVariables {
                env('BRANCH_NAME', branch_name)
            }
            definition {
                cpsScm {
                    scm {
                        git {
                            remote { url(tools_url) }
                            branches(branch_name)
                            scriptPath('smoketests/build_tests/docker/Jenkinsfile')
                            extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                        }
                    }
                }
            }
        }


    }
    catch (Exception ex) {
        println("createSmoktestTemplateHelm() Exception: ${ex.toString()}")
        return false
    }
    return true
}


boolean main()
{
    String path_prefix = getPathPrefix(branch_name, delivery_branch)
    boolean create_smoketest_root = createSmoktestRoot(path_prefix)
    if (create_smoketest_root) {
        println("Create Smoketest Root: SUCCESS")
    }
    else
    {
        println("Create Smoketest Root: FAILURE")
        return false
    }
    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}

