#!/usr/bin/env python3
import yaml
import sys

KEY_TENANTS='tenants'
KEY_GLOBAL='global'
KEY_UTILITIES='utilities'

REGEX_MATCH_ALL=".*"
REGEX_ONLY_MAIN="'^(.*main).*$'"
REGEX_EXCLUDE_MAIN="^(?!.*main).*$"

TENANT_PIPELINE='Pipeline'
TENANT_GROUP_A="Group-A"
TENANT_GROUP_B="Group-B"
TENANT_TEST_1="Test-1"
TENANT_TEST_2="Test-2"

def setGlobalVar(config, var_name, val):
    if KEY_GLOBAL not in config:
        config[KEY_GLOBAL] = {}
    config[KEY_GLOBAL][var_name] = val

def setBitbucketUrl(config, bitbucket_url):
    setGlobalVar(config, 'bitbucket_url', bitbucket_url)


def templateTenant (tenant_name, perm_groups, project_list, filter_repo_regex):
    if type(perm_groups) != list:
        raise Exception('Invalid permission group type passed')
    if type(project_list) != list:
        raise Exception('Invalid project list type passed')
    tenant_config = { 'tenant_name' : tenant_name,
                      'perm_groups' : perm_groups,
                      'project_list' : project_list,
                      'filter_repo_regex' : filter_repo_regex,
                    }
    return tenant_config



def templateJob(job_name, display_name, desc, branch_filter_regex=REGEX_MATCH_ALL, branch_build_regex=REGEX_MATCH_ALL):
    return { 'job_name' : job_name,
             'display_name' : display_name,
             'description' : desc,
             'branch_filter_regex' : branch_filter_regex,
             'branch_build_regex' : branch_filter_regex,
            }

def addTentant(config, tenant_name, perm_groups, project_list, filter_repo_regex = REGEX_MATCH_ALL):
    if KEY_TENANTS not in config:
        config[KEY_TENANTS] = []
    tenant_config = templateTenant(tenant_name, perm_groups, project_list, filter_repo_regex)
    print(f'Creating Tentant {tenant_name}')
    config[KEY_TENANTS].append(tenant_config)

def createCommonUtilityJobs(config):
    setGlobalVar(config, KEY_UTILITIES, [])

def createAllTenants(config):
    addTentant(config=config,
               tenant_name=TENANT_PIPELINE,
               perm_groups=['pipline','admin'],
               project_list=['PIP','PIPAPP'],
               filter_repo_regex='^(?!.*(jenkins-shared-lib))'
    )

    addTentant(config=config,
               tenant_name=TENANT_GROUP_A,
               perm_groups=['groupa'],
               project_list=['prjA'],
    )

    addTentant(config=config,
               tenant_name=TENANT_GROUP_B,
               perm_groups=['groupb'],
               project_list=['prjB'],
    )

    addTentant(config=config,
               tenant_name=TENANT_TEST_1,
               perm_groups=['test1'],
               project_list=['prjC'],
    )

    addTentant(config=config,
               tenant_name=TENANT_TEST_2,
               perm_groups=['test2'],
               project_list=['prjD'],
    )





def main():
    output_path = 'config/config.yaml'
    output_config = {}

    try:
        setBitbucketUrl(output_config, "http://10.0.0.35")
        createCommonUtilityJobs(output_config)
        createAllTenants(output_config)

        with open('config/config.yaml', 'w') as f:
            yaml.dump(output_config, f)
    except Exception as ex:
        print("Exception building config: %s" % ex)
        sys.exit(1)


if __name__ == '__main__':
    main()
    sys.exit(0)