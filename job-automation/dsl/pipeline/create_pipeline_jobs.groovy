import groovy.transform.Field
@Field final String PIPELINE_PATH = 'pipeline'

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

boolean createPipelineRootFolder(String path_prefix)
{
    try
    {
        folder("${path_prefix}/${PIPELINE_PATH}")
        {
            displayName("020 - Jenkins Admin Jobs")
            description("Jenkins Admin jobs Area")

            properties {
                authorizationMatrix {
                    inheritanceStrategy { nonInheriting() }
                }
            }
        }
    }
    catch (Exception ex)
    {
        println("createPipelineRoot() Exception: ${ex.toString()}")
        return false
    }

    return true
}



boolean createDeployJob(String path_prefix)
{
    try
    {
        def tools_url = getBinding().getVariables().getOrDefault('TOOLS_URL', 'NotSet')
        def desc = "Runs all the JobDSL for job deployment"
        def main_branch = branch_name == delivery_branch
        multibranchPipelineJob("${path_prefix}/${PIPELINE_PATH}/deploy-devel-jobs")
        {
            displayName("000 - Deploy Development Jenkins Jobs")
            if (main_branch) {
                description(desc)
            }
            else {
                description(desc + "\n!!! Intentionally ignoring main branch from scanning while on a development branch !!!")
            }

            branchSources {
                branchSource {
                    source {
                        git {
                            remote (tools_url)
                            id ('deploy-devel-jobs-source-id')
                            traits {
                                gitBranchDiscovery()

                                headRegexFilter
                                {
                                    regex('^(?!.*main).*$')
                                }
                            }
                        }
                    }
                    strategy {
                        allBranchesSame {
                            props {
                                suppressAutomaticTriggering {
                                    if (main_branch) {
                                        triggeredBranchesRegex ('^(.*main).*$')
                                    }
                                    else {
                                        triggeredBranchesRegex ('^$.')
                                    }
                                }
                            }
                        }
                    }
                }
            }
            orphanedItemStrategy {
                discardOldItems {
                    numToKeep(0)
                }
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath("Jenkinsfile")
                }
            }
            triggers {
                periodicFolderTrigger {
                    interval("1h")
                }
            }
        }
    }
    catch (Exception ex)
    {
        println("createPipelineRoot() Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean main()
{
    boolean is_delivery_branch = branch_name == delivery_branch
    String path_prefix = getPathPrefix(is_delivery_branch)

    boolean create_root_result = createPipelineRootFolder(path_prefix)

    if (create_root_result == true) {
        println("Create pipeline root folder: SUCCESS")
    }
    else
    {
        println("Create pipeline root folder: FAILURE")
        return false
    }

    boolean create_deploy_job_result = createDeployJob(path_prefix)
    if (create_deploy_job_result)
    {
        println("Create deploy job: SUCCESS")
    }
    else
    {
        println("Create deploy job: FAILURE")
        return false
    }

    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution: FAILURE")
}





