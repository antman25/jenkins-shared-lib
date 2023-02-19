multibranchPipelineJob("deploy-jenkins-jobs")
{
    displayName("000 - Deploy Production Jenkins Jobs")
    description("Use this job to deploy production jenkins jobs")


    branchSources {
        branchSource {
            source {
                git {
                    remote ('https://github.com/antman25/jenkins-shared-lib.git')
                    // all id's must be unique according to docs
                    id ('deploy-production-jobs-source-id')
                    traits {
                        gitBranchDiscovery()

                        headRegexFilter
                        {
                            regex('^(.*main).*$')
                        }
                    }
                }
            }
            strategy {
                allBranchesSame {
                    props {
                        suppressAutomaticTriggering {
                            triggeredBranchesRegex ('^(.*main).*$')
                        }
                    }
                }
            }
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            numToKeep(20)
        }
    }
    factory {
        workflowBranchProjectFactory {
            scriptPath('Jenkinsfile')
        }
    }
    triggers {
        periodicFolderTrigger {
            interval("1h")
        }
    }
}