#!/usr/bin/env python3
import yaml
import sys

def setGlobalVar(config, var_name, val):
    if 'global' not in config:
        config['global'] = {}
    config['global'][var_name] = val


def createTenantDefaults (tenant_name, perm_groups, project_list, filter_repo_regex):
    if type(perm_groups) != list:
        raise Exception('Invalid permission group type passed')
    if type(project_list) != list:
        raise Exception('Invalid project list type passed')
    tenant_config = { 'tenant_name' : tenant_name,
                      'perm_groups' : perm_groups,
                      'project_list' : project_list,
                      'filter_repo_regex' : filter_repo_regex
                    }
    return tenant_config

def addTentant(tenants, tenant_name, perm_groups, project_list, filter_repo_regex = '.*'):
    tenant_config = createTenantDefaults(tenant_name, perm_groups, project_list, filter_repo_regex)
    print("Adding Tentant: %s" % tenant_name)
    tenants.append(tenant_config)

def createAllTenants(config):
    if 'tenants' not in config:
        config['tenants'] = []
    addTentant(config['tenants'], 'Pipeline', ['pipeline'], ['PIP'], '^(?!.*(jenkins-shared-lib))')
    addTentant(config['tenants'], 'Group-A', ['groupA'], ['prjA'])
    addTentant(config['tenants'], 'Group-B', ['groupA', 'groupB'], ['prjB'])
    addTentant(config['tenants'], 'Test-1', ['groupC'], ['prjC'])
    addTentant(config['tenants'], 'Test-2', ['groupD'], ['prjD'])

def main():
    output_path = 'config/config.yaml'
    output_config = {}

    try:
        #setGlobalVar(output_config, 'artifactory_url', 'http://nexus.antlinux.local')
        setGlobalVar(output_config, 'bitbucket_url', 'http://10.0.0.35')
        createAllTenants(output_config)

        with open('config/config.yaml', 'w') as f:
            yaml.dump(output_config, f)
    except Exception as ex:
        print("Exception building config: %s" % ex)
        sys.exit(1)


if __name__ == '__main__':
    main()
    sys.exit(0)