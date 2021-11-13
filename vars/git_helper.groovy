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
	print("Output raw: ${output}")
	def output_lines = output.split('\n')
	//print ("Output Lines: ${output_lines}")
	output_lines.each { line ->
		//print("Line: ${line}")
		def line_split = line.split('\t')
		print("Line Split: ${line_split}")

		if (line_split.size() == 2)
		{
			def ref_name =line_split[1]
			def ref_name_split = ref_name.split('/')
			if (ref_name_split == 2)
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


return this
