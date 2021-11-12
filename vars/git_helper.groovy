def getRemoteHEAD(url, branch)
{
	def cmd = "git ls-remote ${url} ${branch}"
	//print("cmd = ${cmd}")
	def output = cmd.execute().text.trim()
	//print("output = ${output}")
	return output
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


return this
