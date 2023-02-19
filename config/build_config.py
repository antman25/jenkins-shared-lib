#!/usr/bin/env python3
import yaml
import sys

class NoAliasDumper(yaml.SafeDumper):
    def ignore_aliases(self, data):
        return True

KEY_TENANTS='tenants'
KEY_COMMON='common'
KEY_UTILITIES='utilities'
KEY_SANDBOX='sandbox'
KEY_JOBS='jobs'
KEY_ENVVARS='envVars'

REGEX_MATCH_ALL=".*"
#REGEX_EXCLUDE_ALL="^(?!.*).*$"
REGEX_EXCLUDE_ALL="!.*$"
REGEX_ONLY_MAIN="^(.*main).*$"
REGEX_EXCLUDE_MAIN="^(?!.*main).*$"
# Regex notes
# ^(?:.*develop|.*master|.*release/\d+\.\d+\.\d+(?!.))$

JOB_TYPE_DEFAULT='pipelineJob'
JOB_TYPE_MULTIBRANCH='multibranchPipelineJob'

TENANT_CRED_BITBUCKET_RW='tenant-bitbucket-rw-cred'
TENANT_CRED_BITBUCKET_RO='tenant-bitbucket-ro-cred'
TENANT_CRED_ARTIFACTORY_RW='tenant-artifactory-rw-cred'
TENANT_CRED_ARTIFACTORY_RO='tenant-artifactory-ro-cred'

TENANT_PIPELINE='PIPELINE'
TENANT_GROUP_A="group-a"
TENANT_GROUP_B="group-b"
TENANT_TEST_1="test-1"
TENANT_TEST_2="test-2"

DEFAULT_ENV_VARS = {}


def setConfig(config, config_key, var_name, var_value):
    if config_key not in config:
        config[config_key] = {}
    config[config_key][var_name] = var_value

def setCommonJobEnvVar(config, var_name, var_value):
    setConfig(config[KEY_COMMON], KEY_ENVVARS, var_name, var_value)



def setCommonJob(config, job_type, job_name, job_data):
    if KEY_JOBS not in config[KEY_COMMON]:
        setConfig(config, KEY_COMMON, KEY_JOBS, {})
    setConfig(config[KEY_COMMON][KEY_JOBS], job_type, job_name, job_data)

def setTenantJob(config, tenant_name, job_type, job_name, job_data):
    setConfig(config[KEY_TENANTS][tenant_name][KEY_JOBS], job_type, job_name, job_data)

def setBitbucketUrl(config, bitbucket_url):
    setConfig(config, KEY_COMMON, 'urlBitbucket', bitbucket_url)

def setDeliveryBranch(config, delivery_branch):
    setConfig(config, KEY_COMMON, 'branchDelivery', delivery_branch)

def setRootTestingFolder(config, test_path):
    setConfig(config, KEY_COMMON, 'rootTestingFolder', test_path)

def templateMultiBranchJob(display_name, desc, repo_url, jenkinsfile_path, credential_id, job_env_vars=DEFAULT_ENV_VARS, branch_filter_regex=REGEX_MATCH_ALL, branch_build_regex=REGEX_EXCLUDE_ALL):
    return { 'displayName' : display_name,
                 'description' : desc,
                 'repoUrl' : repo_url,
                 'pathJenkinsfile' : jenkinsfile_path,
                 'credentialId' : credential_id,
                 'jobEnvVars' : job_env_vars,
                 'branchFilterRegex' : branch_filter_regex,
                 'branchBuildRegex' : branch_build_regex,
                }
    #job = templateJob(display_name, desc, repo_url, jenkinsfile_path, credential_id)
    #job['branchFilterRegex'] = branch_filter_regex
    #job['branchBuildRegex'] = branch_build_regex
    #job['type'] = JOB_TYPE_MULTIBRANCH
    #return job

def templateJob(display_name, desc, repo_url, jenkinsfile_path, credential_id, job_env_vars=DEFAULT_ENV_VARS):
    return { 'displayName' : display_name,
             'description' : desc,
             'repoUrl' : repo_url,
             'pathJenkinsfile' : jenkinsfile_path,
             'credentialId' : credential_id,
             'jobEnvVars' : job_env_vars,
             'type' : JOB_TYPE_DEFAULT
            }

def templateTenant (tenant_display_name, perm_groups, build_project_list, filter_repo_regex):
    if type(perm_groups) != list:
        raise Exception('Invalid permission group type passed')
    if type(build_project_list) != list:
        raise Exception('Invalid project list type passed')
    tenant_config = { 'displayName' : tenant_display_name,
                      'groups' : perm_groups,
                      'projectList' : build_project_list,
                      'repoFilterRegex' : filter_repo_regex,
                      'jobs' : {},
                    }
    return tenant_config

def addTenant(config, tenant_name, tenant_display_name, perm_groups, build_project_list, filter_repo_regex = REGEX_MATCH_ALL):

    tenant_name = tenant_name.lower()
    if KEY_TENANTS not in config:
        config[KEY_TENANTS] = {}
    if tenant_name in config[KEY_TENANTS]:
        raise ("Attempted to add a duplicate tenant config")
    config[KEY_TENANTS][tenant_name] = templateTenant(tenant_display_name, perm_groups, build_project_list, filter_repo_regex)
    print(f'Creating Tentant {tenant_name}')
    dump_config(config[KEY_TENANTS][tenant_name] )


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

def createCommonJobEnvVars(config):
    setCommonJobEnvVar(config, 'COMMON_VAR1', 'TestValue2')
    setCommonJobEnvVar(config, 'COMMON_VAR2', 'TestValue1')

def createAllTenants(config):
    addTenant(config=config,
               tenant_name=TENANT_PIPELINE,
               tenant_display_name='Pipeline Team',
               perm_groups=['pipline','admin'],
               build_project_list=['PIP','PIPAPP'],
               filter_repo_regex='^(?!.*(jenkins-shared-lib))'
    )
    print(config)
    config[KEY_TENANTS][TENANT_PIPELINE] = {}
    config[KEY_TENANTS][TENANT_PIPELINE][KEY_JOBS] = {}
    setTenantJob(config, TENANT_PIPELINE, "Demo", "test-job1", templateMultiBranchJob(display_name='K8S Template Pipeline',
                                                                                          desc='Demo Job One',
                                                                                          repo_url="",
                                                                                          jenkinsfile_path='jenkinsfile/common/utilities/task-one/Jenkinsfile',
                                                                                          credential_id=TENANT_CRED_BITBUCKET_RO,
                                                                                          job_env_vars={'EnvVar1' : True}
                                                                    ))


    addTenant(config=config,
               tenant_name=TENANT_GROUP_A,
               tenant_display_name=TENANT_GROUP_A,
               perm_groups=['groupa'],
               build_project_list=['prjA']
    )

    addTenant(config=config,
               tenant_name=TENANT_GROUP_B,
               tenant_display_name=TENANT_GROUP_B,
               perm_groups=['groupb'],
               build_project_list=['prjB']
    )

    addTenant(config=config,
               tenant_name=TENANT_TEST_1,
               tenant_display_name="Team One",
               perm_groups=['test1'],
               build_project_list=['prjC']
    )

    addTenant(config=config,
               tenant_name=TENANT_TEST_2,
               tenant_display_name="Team Two",
               perm_groups=['test2'],
               build_project_list=['prjD']
    )

def createCommonUtilityJobs(config, tools_url):
    # Utility Types
    setCommonJob(config, KEY_UTILITIES, "release-management", templateMultiBranchJob(display_name='Release Management',
                                                                                      desc='Release sw',
                                                                                      repo_url=tools_url,
                                                                                      jenkinsfile_path='jenkinsfile/common/utilities/release-management/Jenkinsfile',
                                                                                      credential_id=TENANT_CRED_BITBUCKET_RO,
                                                                                      job_env_vars={'REL_MGMT' : True}
                                                                            ))

    setCommonJob(config, KEY_UTILITIES, "common-util-job1", templateMultiBranchJob(display_name='Util Job One',
                                                                                  desc='Util Job One',
                                                                                  repo_url=tools_url,
                                                                                  jenkinsfile_path='jenkinsfile/common/utilities/task-one/Jenkinsfile',
                                                                                  credential_id=TENANT_CRED_BITBUCKET_RO,
                                                                                  job_env_vars={'EnvVar1' : True}
                                                            ))
    setCommonJob(config, KEY_UTILITIES, "common-util-job2", templateMultiBranchJob(display_name='Util Job Two',
                                                                                  desc='Release sw',
                                                                                  repo_url=tools_url,
                                                                                  jenkinsfile_path='jenkinsfile/common/utilities/task-two/Jenkinsfile',
                                                                                  credential_id=TENANT_CRED_BITBUCKET_RO,
                                                                                  job_env_vars={'EnvVar2' : 'blah', 'EnvaVar3' : False}
                                                            ))

    # Demo
    setCommonJob(config, "DemoType", "common-demo-job1", templateMultiBranchJob(display_name='Demo Job One',
                                                                                      desc='Demo Job One',
                                                                                      repo_url=tools_url,
                                                                                      jenkinsfile_path='jenkinsfile/common/utilities/task-one/Jenkinsfile',
                                                                                      credential_id=TENANT_CRED_BITBUCKET_RO,
                                                                                      job_env_vars={'EnvVar1' : True}
                                                                ))


def main():
    #try:
    output_path = 'config/config.yaml'
    output_config = {}

    casc_path = 'casc/baseline.yaml'
    casc_config = {}
    with open(casc_path, 'r') as f:
        casc_config = yaml.safe_load(f)
    tools_url = getJenkinsGlobalEnvVar(casc_config, 'TOOLS_URL')
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
    setDeliveryBranch(output_config, 'main')
    setRootTestingFolder(output_config, 'job-testing')

    createCommonJobEnvVars(output_config)
    createCommonUtilityJobs(output_config, tools_url)
    createAllTenants(output_config)


    with open('config/config.yaml', 'w') as f:
        yaml.dump(output_config, f, Dumper=NoAliasDumper,sort_keys=False)


if __name__ == '__main__':
    main()
    sys.exit(0)