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
}

folder("${pipeline_root_folder}/${job_testing_folder}")
{
    displayName("010 - Job DSL Testing Area")
    description("Spot to test job dsl code prior to delivery")
}


