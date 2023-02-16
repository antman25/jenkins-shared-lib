def createJob(String name, seed_url


multibranchPipelineJob("seed-job-github")
        {
            displayName("TEMPORARY - Deploy Jenkins Jobs - Github - TEMPORARY")
            description("Delete this job when when the jenkins instance is bootstrapped")

            properties {
                folderProperties {
                    properties {
                        stringProperty {
                            key('SEED_URL')
                            value('')
                        }
                    }
                }
            }

            branchSources {
                branchSource {
                    source {
                        git {
                            remote ('https://github.com/antman25/jenkins-shared-lib.git')
                            // all id's must be unique according to docs
                            id ('deploy-jobs-github-source-id')
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
                    interval("1m")
                }
            }
        }