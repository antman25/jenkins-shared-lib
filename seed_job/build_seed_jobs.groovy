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
    displayName("Deploy Jenkins Jobs")
    description("Runs all the JobDSL for job deployment")
}


folder("${pipeline_root_folder}/${job_testing_folder}")
{
    displayName("Job DSL Testing Area")
    description("Spot to test job dsl prior to delivery")
}


