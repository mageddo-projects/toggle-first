#!/bin/bash

set -e

validateRelease(){
	APP_VERSION=$(./gradlew -q version)
	if git rev-parse "$APP_VERSION^{}" >/dev/null 2>&1; then
		echo "> Version already exists $APP_VERSION"
		exit 0
	fi
}

case $1 in

	deploy )

		echo '> deploying'

		validateRelease

		# preparing gpg key
		mkdir -p $HOME/.gnupg/
		openssl aes-256-cbc -d -k ${ENCRYPTION_KEY} -in files/deployment/.gnupg/secring.gpg -out $HOME/.gnupg/secring.gpg

		# building and deploying to sonatype nexys
		export GRADLE_PROJECT_OPTS="-PossrhUsername=mageddo -PossrhPassword=${SIGNING_PASSWORD}"
		export GRADLE_PROJECT_OPTS="${GRADLE_PROJECT_OPTS} -Psigning.password=${SIGNING_PASSWORD} -Psigning.keyId=${SIGNING_KEY_ID}"
		export GRADLE_PROJECT_OPTS="${GRADLE_PROJECT_OPTS} -Psigning.secretKeyRingFile=$HOME/.gnupg/secring.gpg"

		./gradlew build uploadArchives ${GRADLE_PROJECT_OPTS}
		./gradlew closeAndReleaseRepository ${GRADLE_PROJECT_OPTS}

		# publishing tag
		REMOTE="https://${REPO_TOKEN}@github.com/mageddo/toggle-first.git"
		APP_VERSION=$(./gradlew -q version)
		git tag ${APP_VERSION}
		git push "$REMOTE" --tags
		git status
		echo "> Branch pushed. branch=${CURRENT_BRANCH}, version=${APP_VERSION}"

	;;

esac
