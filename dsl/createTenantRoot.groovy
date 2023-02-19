import groovy.transform.Field

@Field final String BUILDJOB_PATH = 'builds'
//@Field final String UTILITIES_PATH = 'utilities'
@Field final String SANDBOX_PATH = 'sandbox'

Map mergeMaps(Map lhs, Map rhs) {
    rhs.each { k, v ->
        lhs[k] = (lhs[k] in Map ? mergeMaps(lhs[k], v) : v)
    }
    return lhs
}

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
            displayName(display_name)
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

def templatePipelineJob(String job_path, String display_name, String desc, String jenkinsfile_url, String jenkinsfile_path, String cred_id)
{
    pipelineJob(job_path) {
        displayName(display_name)
        description(desc)

        environmentVariables {
            env('BRANCH_NAME', branch_name_raw)
        }
        properties {
            disableConcurrentBuilds {}
        }

        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url(jenkinsfile_url)
                            credentials(cred_id)
                        }
                        branches(branch_name_raw)
                        scriptPath(jenkinsfile_path)
                        extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                    }
                }
            }
        }
    }
}

def templateMultibranchPipeline(String job_path, String display_name, String desc, String jenkinsfile_url, String jenkinsfile_path, String cred_id, String branch_filter_regex='.*', String auto_build_regex='.*' ) {
    try {
        multibranchPipelineJob(job_path)
        {
            displayName(display_name)
            description(desc)

            if (is_delivery_branch == false)
            {
                configure {
                    it / disabled << 'true'
                }
            }


            branchSources {
                branchSource {
                    source {
                        git {
                            remote(jenkinsfile_url)
                            // all id's must be unique according to docs
                            //id('deploy-production-jobs-source-id')
                            if (cred_id != "")
                            {
                                //credentialsId(cred_id)
                            }

                            traits {
                                gitBranchDiscovery()

                                headRegexFilter
                                {
                                    //regex('^(.*main).*$')
                                    regex(branch_filter_regex)
                                }
                            }
                        }
                    }
                    strategy {
                        allBranchesSame {
                            props {
                                suppressAutomaticTriggering {
                                    //triggeredBranchesRegex('^(.*main).*$')
                                    triggeredBranchesRegex(auto_build_regex)
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
        println("Exception: ${ex.toString()}")
        return false
    }
    return true
}



boolean createTenantJobs() {

    try {
        def artifactory_url = ""
        def common_jobs = null
        if (config_yaml.containsKey('common') == true) {
            def common_cfg = config_yaml['common']
            bitbucket_url = common_cfg.get('urlBitbucket')
            common_jobs = common_cfg.get('jobs')

        }

        if (config_yaml.containsKey('tenants') == true) {
            def tenants = config_yaml['tenants']

            tenants.each { tenant_name, cur_tenant ->
                def groups = cur_tenant.get('groups')
                def display_name = cur_tenant.get('displayName')
                def project_list = cur_tenant.get('projectList')
                createTenantFolder(path_prefix, tenant_name, display_name, groups)

                def tenant_root_path = "${path_prefix}/${tenant_name}"
                folder("${tenant_root_path}/${BUILDJOB_PATH}")
                {
                    displayName("Builds")
                }

                folder("${tenant_root_path}/${SANDBOX_PATH}")
                {
                    displayName("Sandbox")
                }

                project_list.each { cur_proj_key ->
                    createTentantProjectFolder("${tenant_root_path}/${BUILDJOB_PATH}", bitbucket_url, cur_proj_key)
                }

                Map combined_jobs = mergeMaps(cur_tenant.get('jobs'), common_jobs)
                //println("Combined: ${combined_jobs}")

                combined_jobs.each { job_type, job_list ->
                    folder("${tenant_root_path}/${job_type}")
                    {
                        displayName(job_type)
                    }

                    job_list.each { job_name, job_data ->
                        def job_display_name = job_data.get("displayName")
                        def job_desc = job_data.get("description")
                        def jenkinsfile_url = job_data.get("repoUrl")
                        def jenkinsfile_path = job_data.get("pathJenkinsfile")
                        def job_cred_id = job_data.get("credentialId")
                        def branch_filter_regex = job_data.get("branchFilterRegex")
                        def branch_build_regex = job_data.get("branchBuildRegex")
                        branch_filter_regex = '^(.*' + branch_name_raw + ').*$'
                        branch_build_regex = '!.*$'
                        //templatePipelineJob("${tenant_root_path}/${job_type}/${job_name}", job_display_name, job_desc, jenkinsfile_url, jenkinsfile_path, job_cred_id)
                        templateMultibranchPipeline("${tenant_root_path}/${job_type}/${job_name}", job_display_name, job_desc, jenkinsfile_url, jenkinsfile_path, job_cred_id, branch_filter_regex, branch_build_regex)
                    }
                }
            }
        }
    }
    catch (Exception ex) {
        println("createTenantRoot(): Exception ${ex.toString()}")
        return false
    }

    return true
}



boolean createTentantProjectFolder(String path, String bitbucket_url, String project)
{
    try {
        organizationFolder("${path}/${project}")
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




