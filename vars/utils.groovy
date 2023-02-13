String sanitizeBranchName(String branch_name) {
  return branch_name.replaceAll(/[^\w]/, '-').toLowerCase()
}

Map envVarExists(String key) {
  return env.getProperty(key) != null
}

String getShortCommit()
{
  return sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
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
    def pipeline_root = env.getEnvironment().getOrDefault('PIPELINE_ROOT', 'pipeline-root')
    def job_testing_root = env.getEnvironment().getOrDefault('JOB_TESTING_ROOT', 'job-testing')
    return "${pipeline_root}/${job_testing_root}/${branch_name}"
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

Map getConfig(key = null) {
    String branch_name = env.getEnvironment().getOrDefault('BRANCH_NAME', 'main')
    String branch_name_safe = utils.sanitizeBranchName(branch_name)
    String delivery_branch = env.getEnvironment().getOrDefault('DELIVERY_BRANCH', 'main')
    String chart_path = env.getEnvironment().getOrDefault('CHART_PATH', './helm')
    String dockerfile_path = env.getEnvironment().getOrDefault('DOCKERFILE_PATH', '.')
    String agent_pvc_name = env.getEnvironment().getOrDefault('AGENT_PVC_NAME', '.')
    String pipeline_root = env.getEnvironment().getOrDefault('PIPELINE_ROOT', 'pipeline-root')


  def isDeliveryBranch = branch_name == delivery_branch

  def config = [   branch_name : branch_name,
                                branch_name_safe : branch_name_safe,
                                branchDelivery: branchDelivery,
                                isDeliveryBranch: isDeliveryBranch,
                                chartPath : chart_path,
                                dockerfilePath: dockerfile_path,
                                agentPvcName: agent_pvc_name,
                                folderPipelineRoot: pipeline_root
                            ]



  return key ? config[key] : config

}
