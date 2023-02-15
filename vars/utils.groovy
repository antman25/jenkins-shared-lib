String sanitizeBranchName(String branch_name) {
  return branch_name.replaceAll(/[^\w]/, '-').toLowerCase()
}

Map envVarExists(String key) {
  return env.getProperty(key) != null
}

String getLongCommit()
{
    return sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
}

String getShortCommit()
{
    //return sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
    return getLongCommit().take(8)
}


boolean toBoolean(def value) {
  return (value instanceof java.lang.String) ? value.toBoolean() : value
}

String getPathPrefix(String branch_name, String delivery_branch)
{
  if (branch_name == delivery_branch)
  {
    return ""
  }
  else
  {
    def job_testing_root = env.getEnvironment().getOrDefault('JOB_TESTING_ROOT', 'job-testing')
    return "${job_testing_root}/${branch_name}"
  }
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
                                helmCredentiasl : "${tenant}_ARTIFACTORY_CRED}",
                                dockerfilePath: dockerfile_path,
                                agentPvcName: agent_pvc_name,
                                folderPipelineRoot: pipeline_root
                            ]



  return key ? config[key] : config

}
