def getRemoteHEAD(url, branch)
{
	def cmd = "git ls-remote ${url} ${branch}"
	print("cmd = ${cmd}")
	def output = cmd.execute().text
	print("output = ${output}")
	return output
}

return this
