def sourceInfo = [$class: 'GitSCMSource', remote: "${TOOLS_URL}", credentialsId: "${TENANT}_BITBUCKET_CRED"]
try {
    println("Attempting to load shared lib from branch: ${BRANCH_NAME}")
    library identifier: "jenkins-shared-lib@${BRANCH_NAME}", retriever: modernSCM(sourceInfo)
} catch (err) {
    println("Problem using BRANCH_NAME variable, trying main")
    library identifier: "jenkins-shared-lib@main" retriever: modernSCM(sourceInfo)
}

createBuildJobs()