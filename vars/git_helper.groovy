def getRemoteHEAD(url, branch)
{
	def output = "git ls-remote ${url} ${branch}".execute().text
	return output
}

return this
