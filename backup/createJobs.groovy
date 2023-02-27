import groovy.transform.Field
@Field final String TENANT = 'pipeline'
@Field final String UTILITIES_PATH = 'utilities'

String getPathPrefix(boolean isPrimaryBranch)
{
    if (isPrimaryBranch == true)
    {
        return ""
    }
    else
    {
        return "/${job_testing_folder}/${branch_name}"
    }
}

boolean createDeployJob(String path_prefix)
{
    try
    {
        def desc = "Runs all the JobDSL for job deployment"
        def isPrimaryBranch = branch_name == delivery_branch
        multibranchPipelineJob("${path_prefix}/${TENANT}/${UTILITIES_PATH}/deploy-jobs-git")
        {
            displayName("000 - Deploy Jenkins Jobs - Gitlab")
            if (isPrimaryBranch) {
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
                                    //regex('^(?!.*main).*$')
                                    regex('.*')
                                }
                            }
                        }
                    }
                    strategy {
                        allBranchesSame {
                            props {
                                suppressAutomaticTriggering {
                                    if (isPrimaryBranch) {
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
                workflowBranchProjectFactory {jenkinsfile
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
    boolean isPrimaryBranch = branch_name == delivery_branch
    String path_prefix = getPathPrefix(isPrimaryBranch)

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





