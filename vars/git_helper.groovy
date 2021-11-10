def getRemoteHEAD(url, branch)
{
	def output = "git ls-remote ${url} ${branch}".execute().text
	print("output = ${output}")
	return output
}

return this
