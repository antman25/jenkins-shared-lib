def createJob(String name, seed_url


multibranchPipelineJob("seed-job-github")
{
    displayName("TEMPORARY - Deploy Jenkins Jobs - Github - TEMPORARY")
    description("Delete this job when when the jenkins instance is bootstrapped")

    branchSources {
        branchSource {
            source {
                git {
                    remote ('https://github.com/antman25/jenkins-shared-lib.git')
                    // all id's must be unique according to docs
                    //id ('deploy-jobs-github-source-id')
                    traits {
                        gitBranchDiscovery()

                        headRegexFilter
                        {
                            regex('.*')
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
}