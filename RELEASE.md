
# Release Process

Releases are published to GitHub and Maven Central by JReleaser from a version tag.

## Prerequisites

- Push access to the `codejive/java-properties` repository.
- A Maven Central publisher account and namespace for `org.codejive`.
- An armored GPG key configured in the repository's `jreleaser` environment:
  `JRELEASER_GPG_PUBLIC_KEY`, `JRELEASER_GPG_SECRET_KEY`, and
  `JRELEASER_GPG_PASSPHRASE`.
- The `MAVENCENTRAL_USERNAME` and `MAVENCENTRAL_PASSWORD` secrets.
- The workflow-provided `GITHUB_TOKEN` permission to create releases.

## Creating a Release

1. Ensure the changes are committed and the CI workflow is green.
2. Create and push an annotated tag whose name is the release version, for example:

	```shell
	git tag -a v0.0.8 -m "Release v0.0.8"
	git push origin v0.0.8
	```

3. The `Release` workflow sets the Maven version from the tag, builds the sources and
	Javadoc artifacts, and runs JReleaser.
4. Verify the GitHub release and the published component on Maven Central.

The workflow can also be started manually from GitHub Actions. In that case, provide
the version without the leading `v`.

## Local Configuration Check

This validates the JReleaser configuration without publishing anything:

```
./mvnw jreleaser:config
```

To build the artifacts that a release would stage locally:

```
./mvnw -B -Prelease clean package
```

Publishing should be performed by the GitHub Actions workflow so that credentials and
signing keys remain outside the local environment.

## Recovery

```
./mvnw jreleaser:full-release
```

Re-running the workflow for the same tag is safe because the JReleaser GitHub release
configuration allows overwriting an existing release. If a release must be corrected,
fix the source, create a new patch version, and push a new tag.

```
mvn -B release:update-versions -DdevelopmentVersion=1.2.3-SNAPSHOT
```

A manual deploy can be done like this:

```
mvn clean deploy -P release
```
