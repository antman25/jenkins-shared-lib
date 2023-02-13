#!/usr/bin/env python3
import yaml

def setGlobalVar(config, var_name, val):
    if 'global' not in config:
        config['global'] = {}
    config['global'][var_name] = val


def createTenantDefaults (tenant_name, perm_groups, project_list):
    if perm_groups is not list:
        raise Exception('Invalid permission group type passed')
    if project_list is not list:
        raise Exception('Invalid project list type passed')
    tenant_config = { 'tenant_name' : tenant_name,
                          'perm_groups' : perm_groups,
                          'project_list' : project_list
                        }
    return tenant_config

def createAllTenants(config):
    if 'tenants' not in config:
        config['tenants'] = []
    config['tenants'].append(createTenantDefaults('tenA', ['groupA'], ['prjA']))


def main():
    output_path = 'config/config.yaml'
    output_config = {}

    try:
        setGlobalVar(output_config, 'artifactory_url', 'http://nexus.antlinux.local')
        setGlobalVar(output_config, 'bitbucket_url', 'http://bitbucket.antlinux.local')
        createAllTenants(output_config)

        with open('config/config_test.yaml', 'w') as f:
            yaml.dump(output_config, f)
    except Exception as ex:
        print("Exception building config: %s" % ex)


if __name__ == '__main__':
    main()