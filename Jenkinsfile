/*try
{
    library "jenkins-shared-lib@${BRANCH_NAME}"
    println("Loaded jenkins-shared-lib from branch: ${BRANCH_NAME}")
}
catch (Exception ex)
{
    println("Problem using BRANCH_NAME, trying main")
    library "jenkins-shared-lib@main"
}*/

def sourceInfo = [$class: 'GitSCMSource', remote: "${BITBUCKET_URL}/scm/${BUILD_BITBUCKET_PROJECT}/jenkins-automation.git", credentialsId: "${TENANT}_BITBUCKET_CRED"]
try {
    library identifier: "jenkins-automation@${BRANCH_NAME}" retriever: modernSCM(sourceInfo)
} catch (err) {
    library identifier: "jenkins-automation@main" retriever: modernSCM(sourceInfo)
}


createBuildJobs()