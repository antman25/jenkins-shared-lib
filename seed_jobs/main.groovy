import org.yaml.snakeyaml.Yaml

folder('test folder')
folder('test folder2')

def env_vars = env.getEnvironment()
println("Env Vars: ${env_vars}")

def yaml = new org.yaml.snakeyaml.Yaml(new FileReader("..//seed_jobs/config.yaml"))
println("YAML: ${yaml}")