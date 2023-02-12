String getPathPrefix(String branch_name, String delivery_branch)
{
    if (branch_name == delivery_branch)
    {
        return "/${pipeline_root_folder}"
    }
    else
    {
        return "${root_folder}/${job_testing_folder}/${branch_name}"
    }
}

def path_prefix = getPathPrefix(branch_name, )

folder("/${pipeline_root_folder}")
{
    displayName("Pipeline Admin")
    description("Pipeline Admin jobs Area")

    properties {
        authorizationMatrix {
            inheritanceStrategy { nonInheriting() }
        }
    }
}

multibranchPipelineJob("/${pipeline_root_folder}/job_deploy")
{
    displayName("000 - Deploy Jenkins Jobs")
    description("Runs all the JobDSL for job deployment")

    branchSources {
        git {
            remote(tools_url)
            // branch source id must be unique
            id ('pipeline-root-job-deploy-branch-source')

        }
    }
    orphanedItemStrategy {
        discardOldItems {
            numToKeep(20)
        }
    }
    factory {
        workflowBranchProjectFactory {
            scriptPath("job_automation/Jenkinsfile")
        }
    }
    properties{
        suppressFolderAutomaticTriggering {
            // Defines a regular expression of branch names which will be triggered automatically, for example (?!
            branches("(?!main.*)")
            // Determines events for which branches with matched names should not be triggered automatically.
            strategy("INDEXING")
        }
    }

}

folder("${pipeline_root_folder}/${job_testing_folder}")
{
    displayName("010 - Job DSL Testing Area")
    description("Spot to test job dsl code prior to delivery")
}


