#!/bin/bash

case $1 in

	deploy )

		validateRelease

		# preparing gpg key
		mkdir -p $HOME/.gnupg/
		openssl aes-256-cbc -d -k ${ENCRYPTION_KEY} -in files/deployment/.gnupg/secring.gpg -out $HOME/.gnupg/secring.gpg

		# building and deploying to sonatype nexys
		./gradlew build uploadArchives -PossrhUsername=mageddo -PossrhPassword=${SIGNING_PASSWORD} \
			-Psigning.password=${SIGNING_PASSWORD} -Psigning.keyId=${SIGNING_KEY_ID} \
			-Psigning.secretKeyRingFile=$HOME/.gnupg/secring.gpg

		# publishing tag
		REMOTE="https://${REPO_TOKEN}@github.com/${REPO_URL}.git"

		git checkout -b build_branch ${CURRENT_BRANCH}
		echo "> Repository added, currentBranch=${CURRENT_BRANCH}"

		git commit -am "Releasing ${APP_VERSION}" || true
		git tag ${APP_VERSION}
		git push "$REMOTE" "build_branch:${CURRENT_BRANCH}"
		git status
		echo "> Branch pushed - Branch $CURRENT_BRANCH"

	;;

esac

validateRelease(){
	APP_VERSION=$(./gradlew -q version)
	if git rev-parse "$APP_VERSION^{}" >/dev/null 2>&1; then
		echo "> Version already exists $APP_VERSION"
		exit 3
	fi
}
