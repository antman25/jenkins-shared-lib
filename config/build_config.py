#!/usr/bin/env python3
import yaml
import sys

KEY_TENANTS='tenants'
KEY_COMMON='common'
KEY_UTILITIES='utilities'


REGEX_MATCH_ALL=".*"
REGEX_EXCLUDE_ALL="^(?!.*).*$"
REGEX_ONLY_MAIN="^(.*main).*$"
REGEX_EXCLUDE_MAIN="^(?!.*main).*$"
# Regex notes
# ^(?:.*develop|.*master|.*release/\d+\.\d+\.\d+(?!.))$


TENANT_CRED_BITBUCKET_RW='tenant-bitbucket-rw-cred'
TENANT_CRED_BITBUCKET_RO='tenant-bitbucket-ro-cred'
TENANT_CRED_ARTIFACTORY_RW='tenant-artifactory-rw-cred'
TENANT_CRED_ARTIFACTORY_RO='tenant-artifactory-ro-cred'

TENANT_PIPELINE='PIPELINE'
TENANT_GROUP_A="group-a"
TENANT_GROUP_B="group-b"
TENANT_TEST_1="test-1"
TENANT_TEST_2="test-2"

def setCommon(config, var_name, val):
    if KEY_COMMON not in config:
        config[KEY_COMMON] = {}
    config[KEY_COMMON][var_name] = val

def setBitbucketUrl(config, bitbucket_url):
    setCommon(config, 'bitbucket_url', bitbucket_url)


def templateTenant (tenant_name, tenant_display_name, perm_groups, project_list, filter_repo_regex):
    if type(perm_groups) != list:
        raise Exception('Invalid permission group type passed')
    if type(project_list) != list:
        raise Exception('Invalid project list type passed')
    tenant_config = { 'tenant_display_name' : tenant_display_name,
                      'perm_groups' : perm_groups,
                      'project_list' : project_list,
                      'filter_repo_regex' : filter_repo_regex,
                    }
    return tenant_config

def templateJob(display_name, desc, repo_url, jenkinsfile_path, credential_id, job_env_vars, branch_filter_regex=REGEX_MATCH_ALL, branch_build_regex=REGEX_MATCH_ALL):
    return { 'display_name' : display_name,
             'description' : desc,
             'repo_url' : repo_url,
             'jenkinsfile_path' : jenkinsfile_path,
             'credential_id' : credential_id,
             'job_env_vars' : job_env_vars,
             'branch_filter_regex' : branch_filter_regex,
             'branch_build_regex' : branch_build_regex,
            }

def addTenant(config, tenant_name, tenant_display_name, perm_groups, project_list, filter_repo_regex = REGEX_MATCH_ALL):
    tenant_name = tenant_name.lower()
    if KEY_TENANTS not in config:
        config[KEY_TENANTS] = {}
    if tenant_name in config[KEY_TENANTS]:
        raise ("Attempted to add a duplicate tenant config")
    config[KEY_TENANTS][tenant_name] = templateTenant(tenant_name, tenant_display_name, perm_groups, project_list, filter_repo_regex)
    print(f'Creating Tentant {tenant_name}')
    dump_config(config[KEY_TENANTS][tenant_name] )

def createCommonUtilityJobs(config, tools_url):
    utility_jobs = {}

    utility_jobs['release-management'] = templateJob(display_name='Release Management',
                                                     desc='Release sw',
                                                     repo_url=tools_url,
                                                     jenkinsfile_path='jenkinsfile/common/release-management/Jenkinsfile',
                                                     credential_id=TENANT_CRED_BITBUCKET_RO,
                                                     job_env_vars={'REL_MGMT' : True},
                                                     branch_filter_regex=REGEX_ONLY_MAIN,
                                                     branch_build_regex=REGEX_EXCLUDE_ALL
                                        )


    utility_jobs['common-task-1'] = templateJob(display_name='Common Task 1',
                                                desc='common task 1',
                                                repo_url=tools_url,
                                                jenkinsfile_path='jenkinsfile/common/task-one/Jenkinsfile',
                                                credential_id=TENANT_CRED_BITBUCKET_RO,
                                                job_env_vars={},
                                                branch_filter_regex=REGEX_ONLY_MAIN,
                                                branch_build_regex=REGEX_EXCLUDE_ALL
                                    )

    utility_jobs['common-task-2'] = templateJob(display_name='Common Task 2',
                                                desc='common task 2',
                                                repo_url=tools_url,
                                                jenkinsfile_path='jenkinsfile/common/tasak-two/Jenkinsfile',
                                                credential_id=TENANT_CRED_BITBUCKET_RO,
                                                job_env_vars={},
                                                branch_filter_regex=REGEX_ONLY_MAIN,
                                                branch_build_regex=REGEX_EXCLUDE_ALL
                                        )

    setCommon(config, KEY_UTILITIES, utility_jobs)



def createAllTenants(config):
    addTenant(config=config,
               tenant_name=TENANT_PIPELINE,
               tenant_display_name='Pipeline Team',
               perm_groups=['pipline','admin'],
               project_list=['PIP','PIPAPP'],
               filter_repo_regex='^(?!.*(jenkins-shared-lib))'
    )

    addTenant(config=config,
               tenant_name=TENANT_GROUP_A,
               tenant_display_name=TENANT_GROUP_A,
               perm_groups=['groupa'],
               project_list=['prjA']
    )

    addTenant(config=config,
               tenant_name=TENANT_GROUP_B,
               tenant_display_name=TENANT_GROUP_B,
               perm_groups=['groupb'],
               project_list=['prjB']
    )

    addTenant(config=config,
               tenant_name=TENANT_TEST_1,
               tenant_display_name="Team One",
               perm_groups=['test1'],
               project_list=['prjC']
    )

    addTenant(config=config,
               tenant_name=TENANT_TEST_2,
               tenant_display_name="Team Two",
               perm_groups=['test2'],
               project_list=['prjD']
    )



def dump_config(config):
    for cur_key in config:
        print(f'\t{cur_key}={config[cur_key]}')

def getJenkinsGlobalEnvVar(casc_config, var_name):
    if 'jenkins' in casc_config:
        if 'globalNodeProperties' in casc_config['jenkins']:
            if len(casc_config['jenkins']['globalNodeProperties']) > 0:
                if 'envVars' in casc_config['jenkins']['globalNodeProperties'][0]:
                    if 'env' in casc_config['jenkins']['globalNodeProperties'][0]['envVars']:
                        for cur_env_var in casc_config['jenkins']['globalNodeProperties'][0]['envVars']['env']:
                            if 'key' in cur_env_var and 'value' in cur_env_var:
                                if cur_env_var['key'] == var_name:
                                    return cur_env_var['value']
    return None

def getJenkinsBitbucketUrl(casc_config):
    if 'unclassified' in casc_config:
        if 'bitbucketEndpointConfiguration' in casc_config['unclassified']:
            if 'endpoints' in casc_config['unclassified']['bitbucketEndpointConfiguration']:
                if len(casc_config['unclassified']['bitbucketEndpointConfiguration']['endpoints']) > 0:
                    if 'bitbucketServerEndpoint' in casc_config['unclassified']['bitbucketEndpointConfiguration']['endpoints'][0]:
                        if 'serverUrl' in casc_config['unclassified']['bitbucketEndpointConfiguration']['endpoints'][0]['bitbucketServerEndpoint']:
                            return casc_config['unclassified']['bitbucketEndpointConfiguration']['endpoints'][0]['bitbucketServerEndpoint']['serverUrl']
    return None

def getJenkinsToolsUrl(casc_config):
    return getJenkinsGlobalEnvVar(casc_config, 'TOOLS_URL')

def main():
    try:
        output_path = 'config/config.yaml'
        output_config = {}

        casc_path = 'casc/baseline.yaml'
        casc_config = {}
        with open(casc_path, 'r') as f:
            casc_config = yaml.safe_load(f)
        tools_url = getJenkinsToolsUrl(casc_config)
        bitbucket_url = getJenkinsBitbucketUrl(casc_config)
        if tools_url:
            print(f"TOOLS_URL = {tools_url}")
        else:
            print("Problem reading TOOLS_URL")
            exit(1)

        if bitbucket_url:
            print(f"Bitbucket URL = {bitbucket_url}")
        else:
            print("Problem reading bitbucket URL")
            exit(1)

        setBitbucketUrl(output_config, bitbucket_url)
        createCommonUtilityJobs(output_config, tools_url)
        createAllTenants(output_config)
        #createTenantUtilityJobs(output_config)

        with open('config/config.yaml', 'w') as f:
            yaml.dump(output_config, f)
    except Exception as ex:
        print("Exception building config: %s" % ex)
        sys.exit(1)


if __name__ == '__main__':
    main()
    sys.exit(0)