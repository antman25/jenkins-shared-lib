#!/usr/bin/env python3
import yaml

def setGlobalVar(config, var_name, val)
    if 'global' not in config:
        config['global'] = {}
    config['global'][var_name] = val

def main():
    output_path = 'config/config.yaml'
    output_config = {}

    setGlobalVar('artifactory_url', 'http://nexus.antlinux.local')
    setGlobalVar('bitbucket_url', 'http://bitbucket.antlinux.local')

    with open('config/config_test.yaml', 'w') as f:
        yaml.dump(output_config, f)


if __name__ == '__main__':
    main()