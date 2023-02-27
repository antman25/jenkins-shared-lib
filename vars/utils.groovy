String slugify(String input) {
    def result =input.replaceAll(/[^\w\s-]/, '').trim().toLowerCase()
    return input.replaceAll(/[-\s]+/, '-')
}

String isPrimaryBranch(String branchName) {
    if (branchName == 'main' || branchName == 'master' || branchName.startsWith('release'))
        return true
    return false
}

Map envVarExists(String key) {
  return env.getProperty(key) != null
}

String getLongCommit()
{
    return sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
}

def getModifiedFiles(commit_id)
{
    def cmd = "git diff --name-only ${commit_id}"
    def output = cmd.execute().text.trim()
    print("getModifiedFilesStdout: ${output}")
    def file_list = output.split('\n')
    print("getModifiedList: ${file_list}")

    return output
}

boolean toBoolean(def value) {
  return (value instanceof java.lang.String) ? value.toBoolean() : value
}



def default_stages(Closure body)
{
  return {
      stage('Clone Code')
      {
          checkout scm
      }

      stage('Additional Setup')
      {

      }
      body()

      stage('Code Scan')
      {

      }

      stage('Post Cleanup')
      {

      }
  }
}

void dumpConfig(Map config)
{
    String output = ""
    config.each {it ->
        output += "${it}\n"
    }
    println("Config Dump:\n${output}")
}

Map getConfig(key = null) {

    String branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
    String branch_name_safe = sanitizeBranchName(branch_name)
    String delivery_branch = env.getEnvironment().getOrDefault('DELIVERY_BRANCH', 'main')
    //String chart_path = env.getEnvironment().getOrDefault('CHART_PATH', './helm')
    String dockerfile_path = env.getEnvironment().getOrDefault('DOCKERFILE_PATH', '.')
    String agent_pvc_name = env.getEnvironment().getOrDefault('AGENT_PVC_NAME', 'jenkins-agent-pvc')
    String pipeline_root = env.getEnvironment().getOrDefault('PIPELINE_ROOT', 'pipeline-root')
    String tenant = "default";

    withFolderProperties {
        tenant = "${env.TENANT}"
    }
  def config = [   branchName : branch_name,
                                branchNameSafe : branch_name_safe,
                                branchDelivery: delivery_branch,
                                isDeliveryBranch: branch_name == delivery_branch,
                                tenant : tenant,
                                agentPvcName: agent_pvc_name,
                                folderPipelineRoot: pipeline_root
                            ]



  return key ? config[key] : config

}
