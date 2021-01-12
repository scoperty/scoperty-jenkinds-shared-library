/*
 * A set of reusable functions for Scoperty Jenkins pipelines.
 * !!! IMPORTANT : This is a public repo, please do not include any confidential information here. !!!
 */

import java.net.URLEncoder

def getBranchType(String branchName) {
	def devPattern = ".*develop|scoperty20"
	def releasePattern = ".*release/.*"
	def featurePattern = ".*feature/.*"
	def hotfixPattern = ".*hotfix/.*"
	def masterPattern = ".*master"
	def pullRequest = ".*PR.*"
	def penTestPattern = "pentest2020"
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
	} else if (branchName =~ penTestPattern) {
		return "penTest"
	} else {
		return null;
	}
}

def getDeploymentEnvironment(String branchType) {
	if (branchType == "pullRequest") {
		return "none"
	} else if (branchType == "dev") {
		return "dev"
	} else if (branchType == "release") {
		return "stg"
	} else if (branchType == "master") {
		return "prd"
	} else if (branchType == "penTest") {
		return "tst"
	} else {
		return "none";
	}
}

def getBuildEnvironment(String deploymentEnvironment) {
	if (deploymentEnvironment == "stg") {
		return "stage"
	}
	if (deploymentEnvironment == "prd") {
		return "prod"
	}
	if (deploymentEnvironment == "tst") {
		return "penTest"
	} else {
		return "dev"
	}
}

def getSonarArguments(String branchType) {
	if (branchType == "pullRequest") {
		return "-Dsonar.pullrequest.base=${CHANGE_TARGET} -Dsonar.pullrequest.branch=${CHANGE_BRANCH} -Dsonar.pullrequest.key=${CHANGE_ID}"
	} else {
		return "-Dsonar.branch.name=${BRANCH_NAME}"
	}
}

def createVirtualEnv(String pythonPath, String name) {
	sh "${pythonPath} -m venv ${name}"
}

def executeIn(String environment, String script) {
	sh "source ${environment}/bin/activate && " + script
}

def encodeURL(String url) {
	return URLEncoder.encode(url, "UTF-8")
}

def getSlackDefaults() {
	def map = [
			SUCCESS : [color: "#3EB991", emoji: ":thumbsup:"],
			UNSTABLE: [color: "#FFC300", emoji: ":thinking_face:"],
			ABORTED : [color: "#E01563", emoji: ":x:"],
			FAILURE : [color: "#E01563", emoji: ":cry:"],
			STARTED : [color: "#6ECADC", emoji: ":crossed_fingers:"]
	]
	return map
}

def boolean hasChangesIn(String module) {
	if (env.CHANGE_TARGET == null) {
		return true;
	}

	def MASTER = sh(
			returnStdout: true,
			script: "git rev-parse origin/${env.CHANGE_TARGET}"
	).trim()

	// Gets commit hash of HEAD commit. Jenkins will try to merge master into
	// HEAD before running checks. If this is a fast-forward merge, HEAD does
	// not change. If it is not a fast-forward merge, a new commit becomes HEAD
	// so we check for the non-master parent commit hash to get the original
	// HEAD. Jenkins does not save this hash in an environment variable.
	def HEAD = sh(
			returnStdout: true,
			script: "git show -s --no-abbrev-commit --pretty=format:%P%n%H%n HEAD | tr ' ' '\n' | grep -v ${MASTER} | head -n 1"
	).trim()

	return sh(
			returnStatus: true,
			script: "git diff --name-only ${MASTER}...${HEAD} | grep ^${module}/"
	) == 0
}
