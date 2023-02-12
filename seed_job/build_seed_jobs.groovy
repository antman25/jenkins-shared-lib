folder(seed_jobs_root)
{
    displayName("Seed Jobs")
    description("Automated deployment of jenkins jobs")

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