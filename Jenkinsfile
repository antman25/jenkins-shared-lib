def sourceInfo = null
try {
    println("Attempting to load shared lib from branch: ${env.BRANCH_NAME} -- Using ${env.CRED_ID} credentials")

    if ("${CRED_ID}" != '')
        sourceInfo = [$class: 'GitSCMSource', remote: "${TOOLS_URL}", credentialsId: "${CRED_ID}"]
    else
        sourceInfo = [$class: 'GitSCMSource', remote: "${TOOLS_URL}"]

    library identifier: "jenkins-shared-lib@${BRANCH_NAME}", retriever: modernSCM(sourceInfo)
} catch (err) {
    println("Problem using BRANCH_NAME variable, trying main")
    library identifier: "jenkins-shared-lib@main", retriever: modernSCM(sourceInfo)
}

createBuildJobs()