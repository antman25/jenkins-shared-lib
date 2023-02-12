try
{
    library "jenkins-shared-lib@${BRANCH_NAME}"
    println("Loaded jenkins-shared-lib from branch: ${BRANCH_NAME}")
}
catch (Exception ex)
{
    println("Problem using BRANCH_NAME, trying main")
    library "jenkins-shared-lib@main"
}

createBuildJobs()