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
            id ('job-deploy-branch-source')
        }
    }

}


folder("${pipeline_root_folder}/${job_testing_folder}")
{
    displayName("010 - Job DSL Testing Area")
    description("Spot to test job dsl prior to delivery")
}


