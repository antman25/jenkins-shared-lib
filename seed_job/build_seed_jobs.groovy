folder(seed_jobs_root)
{
    displayName("Jenkins Admin Jobs")
    description("Jenkins admin jobs")

    properties {
        authorizationMatrix {
            inheritanceStrategy { nonInheriting() }
        }
    }
}

folder("${seed_jobs_root}/${job_testing_folder}")
{
    displayName("Job Testing")
    description("Test Area")
}