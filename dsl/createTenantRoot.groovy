import groovy.transform.Field

@Field final String BUILDJOB_PATH = 'builds'
//@Field final String UTILITIES_PATH = 'utilities'
//@Field final String SANDBOX_PATH = 'sandbox'

List<String> permissionDeveloper(String group) {
    return ["hudson.model.Item.Read:${group}",
            "hudson.model.Item.Cancel:${group}",
            "hudson.model.Item.Build:${group}",
            "hudson.model.Item.Workspace:${group}"];
}

boolean createTenantFolder(String path_prefix, String tenant_name, String display_name, List<String> perm_groups) {
    try {
        def total_permissions = []
        def folder_path = "${path_prefix}/${tenant_name}"
        if (perm_groups != null) {
            perm_groups.each { cur_perm_group ->
                total_permissions.addAll(permissionDeveloper(cur_perm_group))
            }
            println("Path: ${folder_path} -- Permission Groups: ${perm_groups.toString()}")
        } else {
            println("createTenantFolder(): Permission groups was null")
            return false
        }

        folder(folder_path) {
            displayName(tenant_name)
            properties {
                authorizationMatrix {
                    inheritanceStrategy { nonInheriting() }
                    permissions ( total_permissions )
                }
                folderProperties {
                    properties {
                        stringProperty {
                            key('TENANT')
                            value(tenant_name)
                        }
                    }
                }

                folderCredentialsProperty {
                    domainCredentials {
                        domainCredentials {
                            domain {
                                name(tenant_name)
                                description("Credentials domain for ${tenant_name}")
                            }
                            credentials {
                                usernamePassword {
                                    scope('USER')
                                    id("tenant-bitbucket-rw-cred")
                                    description("Bitbucket credentials for Read-Write account. Tenant: ${tenant_name}")
                                    username("jenkins-${tenant_name}-rw")
                                    password(bootstrap_password)
                                }
                                usernamePassword {
                                    scope('USER')
                                    id("tenant-bitbucket-ro-cred")
                                    description("Bitbucket credentials for Read-Only account. Tenant: ${tenant_name}")
                                    username("jenkins-${tenant_name}-ro")
                                    password(bootstrap_password)
                                }
                                usernamePassword {
                                    scope('USER')
                                    id("tenant-artifactory-rw-cred")
                                    description("Artifactory credentials for Read-Write account. Tenant: ${tenant_name}")
                                    username("jenkins-${tenant_name}-rw")
                                    password(bootstrap_password)
                                }
                                usernamePassword {
                                    scope('USER')
                                    id("tenant-artifactory-ro-cred")
                                    description("Artifactory credentials for Read-Only account. Tenant: ${tenant_name}")
                                    username("jenkins-${tenant_name}-ro")
                                    password(bootstrap_password)
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    catch (Exception ex){
        println("createTenantFolder() Exception: ${ex.toString()}")
        return false
    }
    return true
}

def templateMultibranchPipeline(String job_path, String display_name, String desc, String jenkinsfile_path, String branch_filter_regex='.*', String auto_build_regex='.*' ) {
    try {
        multibranchPipelineJob(job_path)
        {
            displayName(displayName)
            description(desc)

            branchSources {
                branchSource {
                    source {
                        git {
                            remote(tools_url)
                            // all id's must be unique according to docs
                            //id('deploy-production-jobs-source-id')
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
                                    triggeredBranchesRegex('^(.*main).*$')
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
                    scriptPath(jenkinsfile_path)
                }
            }

        }
    }
    catch (Exception ex)
    {
        println("Exception: {ex.toString()}")
        return false
    }
    return true
}



boolean createTenantJobs() {

    try {
        def artifactory_url = ""
        if (config_yaml.containsKey('common') == true) {
            def common_cfg = config_yaml['common']
            bitbucket_url = common_cfg.get('urlBitbucket')
        }

        if (config_yaml.containsKey('tenants') == true) {
            def tenants = config_yaml['tenants']

            tenants.each { tenant_name, cur_tenant ->
                def groups = cur_tenant.get('groups')
                def display_name = cur_tenant.get('displayName')
                createTenantFolder(path_prefix, tenant_name, display_name, groups)
            }
        }
    }
    catch (Exception ex) {
        println("createTenantRoot(): Exception ${ex.toString()}")
        return false
    }

    return true
}



boolean createTentantProjectFolder(String path, String bitbucket_url, String project, String tenant)
{
    try {
        organizationFolder("${path}/${BUILDJOB_PATH}/${project}")
        {
            displayName(project)
            description("Project: ${project}\nBitbucket URL: ${bitbucket_url}")

            organizations {
                bitbucket {
                    //autoRegisterHooks(true)
                    serverUrl(bitbucket_url)
                    repoOwner(project)
                    credentialsId("tenant-bitbucket-ro-cred")
                    traits {
                        bitbucketBranchDiscovery {
                            strategyId(0)
                        }

                    }
                }
            }
        }
    }
    catch (Exception ex) {
        println("createTentantBuildProject() Exception: ${ex.toString()}")
        return false
    }

    return true
}

boolean main()
{
    try
    {
        boolean create_tenant_jobs_result = createTenantJobs()

        if (create_tenant_jobs_result == true)
        {
            println("Create tenant jobs: SUCCESS")
        }
        else
        {
            println("Create tenant jobs: FAILURE")
            return false
        }
    }
    catch (Exception ex)
    {
        println("Exception: ${ex.toString()}")
        return false
    }
    return true
}

boolean result = main()
if (result == false)
{
    throw new Exception("Execution FAILURE")
}




