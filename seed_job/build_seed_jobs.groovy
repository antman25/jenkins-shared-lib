folder(seed_jobs_root)
{
    displayName("Seed Jobs")
    description("Automated deployment of jenkins jobs")
}

folder("${seed_jobs_root}/job_testing")
{
    displayName("Job Testing")
    description("Test Area for job dsl changes")
}