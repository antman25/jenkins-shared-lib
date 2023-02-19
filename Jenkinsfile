def sourceInfo = null
withFolderProperties {
    try {
        println("Attempting to load shared lib from branch: ${env.BRANCH_NAME}")

        if ("${TOOLS_URL}".contains('git'))
            sourceInfo = [$class: 'GitSCMSource', remote: "${TOOLS_URL}"]
        else
            sourceInfo = [$class: 'GitSCMSource', remote: "${TOOLS_URL}", credentialsId: "tenant-bitbucket-ro-cred"]

        library identifier: "jenkins-shared-lib@${BRANCH_NAME}", retriever: modernSCM(sourceInfo)
    } catch (err) {
        println("Problem using BRANCH_NAME variable, trying main")
        library identifier: "jenkins-shared-lib@main", retriever: modernSCM(sourceInfo)
    }
    createBuildJobs()
}

