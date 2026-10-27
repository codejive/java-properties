
# Release Process

Releases are published to GitHub and Maven Central by JReleaser from a version tag.

## Prerequisites

- Push access to the `codejive/java-properties` repository.
- A Maven Central publisher account and namespace for `org.codejive`.
- An armored GPG key configured in the repository's `jreleaser` environment:
    `GPG_PUBLIC_KEY`, `GPG_SECRET_KEY`, and `GPG_PASSPHRASE`.
- The `MAVENCENTRAL_USERNAME` and `MAVENCENTRAL_PASSWORD` secrets.
- The workflow-provided `GITHUB_TOKEN` permission to create releases.

## Creating a Release from GitHub.com

1. Open [github.com/codejive/java-properties/actions](https://github.com/codejive/java-properties/actions).
2. Select **Release** in the workflow list.
3. Click **Run workflow**.
4. Leave the branch set to `main` and enter the release version without a leading
    `v`, for example `0.1.1`.
5. Click **Run workflow** to start the release.
6. Open the newly created workflow run and wait for the `release` job to complete.
7. Open [github.com/codejive/java-properties/releases](https://github.com/codejive/java-properties/releases)
    and verify the new GitHub release and its generated changelog.
8. Verify that the component has been published to Maven Central.

The workflow uses the `jreleaser` environment, so GitHub may pause the job until
any required environment approval is granted.

## Creating a Release from a Tag

1. Ensure the changes are committed and the CI workflow is green.
2. Create and push an annotated tag whose name is the release version, for example:

    ```shell
    git tag -a v0.1.1 -m "Release v0.1.1"
    git push origin v0.1.1
    ```

3. The `Release` workflow sets the Maven version from the tag, builds the sources and
    Javadoc artifacts, and runs JReleaser.
4. Verify the GitHub release and the published component on Maven Central.

After a successful release, the workflow increments the patch version, appends
`-SNAPSHOT`, and commits and pushes `pom.xml` for the next development iteration.
For example, releasing `0.1.1` prepares `0.1.2-SNAPSHOT`. Manual releases update the
selected branch; tag releases update the repository's default branch, which must
contain the tagged commit. The workflow token needs permission to push to that
branch under its branch protection rules.

The workflow preserves newer branch commits and skips an already committed version
bump. If `pom.xml` has otherwise changed since the released commit, or the branch
advances during the push, the preparation step fails without overwriting those
changes. In that case, update and commit the next development version manually;
the published release is already complete.

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

If a workflow fails, open its run in **Actions**, inspect the failed step, correct the
underlying issue, and use **Re-run failed jobs**. If the release must be corrected
after publication, fix the source, choose a new patch version, and run the workflow
again from GitHub.com.
