def test = getBinding().getVariables().get("TOOLS_URL")
println("Test: ${test}")

folder(pipeline_root_folder)
{
    displayName("Pipeline Admin Jobs")
    description("Pipeline admin jobs")

    properties {
        authorizationMatrix {
            inheritanceStrategy { nonInheriting() }
        }
    }
}

folder("${pipeline_root_folder}/${job_testing_folder}")
{
    displayName("Job Testing")
    description("Test Area")
}