/*
 * A set of reusable functions for Scoperty Jenkins pipelines.
 */



String getBranchType(String branchName) {
	def devPattern = ".*develop"
	def releasePattern = ".*release/.*"
	def featurePattern = ".*feature/.*"
	def hotfixPattern = ".*hotfix/.*"
	def masterPattern = ".*master"
	def pullRequest = ".*PR.*"
	if (branchName =~ devPattern) {
		return "dev"
	} else if (branchName =~ releasePattern) {
		return "release"
	} else if (branchName =~ masterPattern) {
		return "master"
	} else if (branchName =~ featurePattern) {
		return "feature"
	} else if (branchName =~ hotfixPattern) {
		return "hotfix"
	} else if (branchName =~ pullRequest) {
		return "pullRequest"
	} else {
		return null;
	}
}

String getDeploymentEnvironment(String branchType) {
	if (branchType == "pullRequest") {
		return "pullRequest"
	} else if (branchType == "dev") {
		return "dev"
	} else if (branchType == "release") {
		return "staging"
	} else if (branchType == "master") {
		return "prod"
	} else {
		return "none";
	}
}

String getBuildEnvironment(String deploymentEnvironment) {
	if (deploymentEnvironment == "pullRequest" || deploymentEnvironment == "none") {
		return "dev"
	} else {
		return deploymentEnvironment
	}
}

String getSonarArguments(String deploymentEnvironment) {
	if (deploymentEnvironment == "pullRequest") {
		return "-Dsonar.pullrequest.base=${CHANGE_TARGET} -Dsonar.pullrequest.branch=${CHANGE_BRANCH} -Dsonar.pullrequest.key=${CHANGE_ID}"
	} else if (deploymentEnvironment == "dev" || deploymentEnvironment == "prod") {
		return "-Dsonar.branch.name=${BRANCH_NAME}"
	} else {
		return ""
	}
}
