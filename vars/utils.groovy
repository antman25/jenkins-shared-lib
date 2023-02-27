String sanitizeBranchName(String branchName) {
    def result =branchName.replaceAll(/[^\w\s-]/, '').trim().lower()
    return branchName.replaceAll(/[-\s]+/, '-')
  //return branch_name.replaceAll(/[^\w]/, '-').toLowerCase()
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

def getRemoteHEAD(url, branch)
{
    def cmd = "git ls-remote ${url} ${branch}"
    //print("cmd = ${cmd}")
    def output = cmd.execute().text.trim()
    //print("output = ${output}")
    return output
}

def getRemoteBranches(url)
{
    def cmd = "git ls-remote ${url} refs/heads/*"
    def output = cmd.execute().text.trim()
    def result = []
    //print("Output raw: ${output}")
    def output_lines = output.split('\n')
    //print ("Output Lines: ${output_lines}")
    output_lines.each { line ->
        //print("Line: ${line}")
        def line_split = line.split('\t')
        //print("Line Split: ${line_split}")

        if (line_split.size() == 2)
        {
            def ref_name =line_split[1]
            def ref_name_split = ref_name.split('/')
            //print("Ref Name Split: ${ref_name_split}")
            if (ref_name_split.size() == 3)
            {
                result.add(ref_name_split[2])
            }

        }
    }
    return result
}

def getLocalHEAD()
{
    def cmd = "git rev-parse HEAD"
    def output = cmd.execute().text.trim()
    return output
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
                                agentPvcName: agent_pvc_name,
                                folderPipelineRoot: pipeline_root
                            ]



  return key ? config[key] : config

}
