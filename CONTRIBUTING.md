<!-- Managed by otterdog blueprint require-contributing. Do not edit here: change it in https://github.com/osgi/.eclipsefdn (otterdog/blueprints). -->

# Contributing to the OSGi Specification Project

Here are instructions to get you started.
They are probably not perfect, please let us know if anything feels wrong or incomplete.

## Project

This repository, [org.osgi.service.log](https://github.com/osgi/org.osgi.service.log), is part of the [OSGi Specification Project](https://projects.eclipse.org/projects/technology.osgi).
Depending on the specification, it holds the API (`api/`), the specification chapter (`spec/`) and the TCK (`tck/`).
The repositories of the other specifications are listed in the [osgi organization](https://github.com/osgi).
The archived [osgi/osgi](https://github.com/osgi/osgi) monorepo keeps the history up to the split and the tags of the earlier releases.

Our primary written communication channel is the project's email list: [osgi-dev@eclipse.org](https://accounts.eclipse.org/mailing-list/osgi-dev).
We also have a [Slack workspace](https://osgiwg.slack.com) for more informal communications.
Contact a project committer for an invite to the Slack workspace.
The [README](README.md#contact) lists all contact channels, including the spec call.

Issues can be reported in the [GitHub issue tracker](https://github.com/osgi/org.osgi.service.log/issues) of this repository.

## Build Environment

You need a JDK and [Apache Maven](https://maven.apache.org/).
CI builds with JDK 21; the API is compiled for Java 8 (`release 8`), so a newer JDK is fine.

- `mvn clean verify` builds everything in the repository and runs the tests and the TCK.
- `mvn -Pjavadoc-check package` checks the javadoc the same way CI does.

The version lives in `.mvn/maven.config`; the parent pom `org.osgi:org.osgi.maven.pom.parent` resolves from Sonatype Central snapshots, which the reactor pom declares.

We use GitHub Actions for continuous integration.
The workflows in `.github/workflows` call reusable workflows in [osgi/.github](https://github.com/osgi/.github), where the CI logic lives.

### Building the Specification

Where the repository has a `spec/` folder, its module renders the chapter as PDF and as HTML.
To build it without running the tests:

```script
mvn -DskipTests package
```

The results are in `spec/*/target/spec/pdf/` and `spec/*/target/spec/html/`, plus a zip of the HTML in `spec/*/target/`.

## IDE Setup

Prerequisites are Eclipse ("Eclipse IDE for Java Developers", which includes Maven support) and [Bndtools](https://bndtools.org/).

Use `Import...` > `Maven` > `Existing Maven Projects` in Eclipse and select the repository root.

### Run / Debug the TCK

Where the repository has a `tck/` folder, the TCK runs in an OSGi framework against a compatible implementation.
The `tck.bndrun` file of each run module under `tck/` configures the framework and the implementation.
To run only the TCK from the command line:

```script
mvn -pl tck -am -amd verify
```

Each run module writes its JUnit report to its `target/test-reports/` folder.

In Eclipse with Bndtools, open a `tck.bndrun` and use `Run As` > `Bnd OSGi Test Launcher (JUnit)`, or `Debug As` with the same launcher.

## Workflow

We use [git triangular workflow](https://github.blog/2015-07-29-git-2-5-including-multiple-worktrees-and-triangular-workflows/).
This means you should not push contributions directly into the main repository, [org.osgi.service.log](https://github.com/osgi/org.osgi.service.log) in the osgi organization.
All contributions should come in through pull requests.
So each contributor will need to [fork the main repository](https://github.com/osgi/org.osgi.service.log/fork) on GitHub.
All contributions are made as commits to your fork.
Then you submit a pull request to have them considered for merging into the main repository.

### Setting up the triangular workflow

After forking the main repository on GitHub, you can clone the main repository to your system:

```script
git clone https://github.com/osgi/org.osgi.service.log.git
```

This will clone the main repository to a local repository on your disk and set up the `origin` remote in Git.
Next you will set up the second side of the triangle to your fork repository.

```script
cd org.osgi.service.log
git remote add fork git@github.com:github-user/org.osgi.service.log.git
```

Make sure to replace the URL with the SSH URL to your fork repository on GitHub.
Then we configure the local repository to push your commits to the fork repository.

```script
git config remote.pushdefault fork
```

So now you will pull from `origin`, the main repository, and push to `fork`, your fork repository.
This option requires at least Git 1.8.4
It is also recommended that you configure

```script
git config push.default simple
```

unless you are already using Git 2.0 where it is the default.

Finally, the third side of the triangle is pull requests from your fork repository to the
main repository.

## Contribution guidelines

For the development of non-trivial new features, this project will first undertake a requirements discussion and, if the requirements discussion concludes successfully, then a design discussion.
By _new feature_, we mean a new specification or a non-trivial enhancement to an existing specification.

Requirements discussions and design discussions should be used whenever there is a benefit to the project for the clarity gained by such discussions.
For a minor enhancement to an existing specification, we can sometimes skip the requirements discussion and include the requirements in the design discussion.

If the design discussion concludes successfully, we can then move to integrate the new feature into the project which requires specification writing and API and TCK development as well as coordination with the development of a [compatible implementation](https://www.eclipse.org/projects/handbook/#specifications-implementations).
Each discussion will occur in its own branch of the git repository (see below for specific details).
This allows the discussion document along with any supporting code to be committed in the branch and for contributors to the discussion to make pull requests against the branch to suggest changes.

The requirement discussion and design discussion documents are stored in the `.design` folder of the repository.
This is done so the folder is at the top of the GitHub repository web page to make it easy to find while generally keeping the folder out of sight during normal development.
From time-to-time, generally when making a specification release, the `.design` folder will be cleaned up to remove older documents whose purpose has been served.

### Requirements discussion

The purpose of the requirements discussion is to engage the project committers in the discussion about the new feature and to more properly understand the terminology, purpose, use cases, and requirements for the new feature.
A new requirements discussion must be started by first opening a new tracking [issue](https://github.com/osgi/org.osgi.service.log/issues) and labeling the issue with the `requirements` label.
Then a new branch should be created from the tip of the `main` branch with the branch name _requirements/XXX_ where _XXX_ is the number of the created tracking issue.

In this new branch, create a `requirements-XXX.md` (or `requirements-XXX.adoc`) requirements document in the `.design` folder.

```script
git checkout -b requirements/XXX main
vi .design/requirements-XXX.md
git add .design/requirements-XXX.md
git commit -s -m "First draft of requirements for new Widget specification"
```

The requirements document should include the following items for the requirements discussion:

- Terminology - The new feature may use terminology new to the project committers or that may have multiple meanings.
- Problem Description - What problem or problems will the new feature address or solve?
- Use Cases - Provide several use cases which can demonstrate the actors and how they will use the new feature to address the problem(s).
- Requirements - This is a list of requirements the new feature is to address.

Once your _requirements/XXX_ branch has your commit with the new requirements document, push this branch to your fork and make a pull request to the main repository.
This pull request will be used to confirm you have signed the [ECA](#legal-considerations) and will be used as the basis for creating the _requirements/XXX_ branch in the main repository.
A project committer must [create the new _requirements/XXX_ branch](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-and-deleting-branches-within-your-repository#creating-a-branch) in the main repository and then [change the base branch of the pull request](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/changing-the-base-branch-of-a-pull-request) to the newly created _requirements/XXX_ branch in the main repository.
At this point, the pull request can be merged into the main repository.

Discussion on the requirements document can take place in the created issue and updates to the requirements document can be made via additional pull requests against the _requirements/XXX_ branch.

To successfully conclude the requirements discussion, the project committers must call a [lazy consensus vote](https://community.apache.org/committers/lazyConsensus.html) of the project committers via an email to the `osgi-dev@eclipse.org` mail list.
The lazy consensus vote must remain open for at least 72 hours.

If the requirements discussion successfully concludes, the _requirements/XXX_ branch is merged into the `main` branch, the tracking issue _XXX_ is closed, and work can then proceed to a design discussion.

### Design discussion

The purpose of the design discussion is to engage the project committers in the discussion about the design for a new feature and its place in the overall OSGi architecture.
We want to ensure that sufficient up-front discussion of a new feature design is done in a branch before committing work to the `main` branch.
A new design discussion must be started by first opening a new tracking [issue](https://github.com/osgi/org.osgi.service.log/issues) and labeling the issue with the `design` label.
Then a new branch should be created from the tip of the `main` branch with the branch name _design/XXX_ where _XXX_ is the number of the created design tracking issue.

In this new branch, create a `design-XXX.md` (or `design-XXX.adoc`) design document in the `.design` folder.

```script
git checkout -b design/XXX main
vi .design/design-XXX.md
git add .design/design-XXX.md
git commit -s -m "First draft of design for new Widget specification"
```

The design document should include the following items for the design discussion:

- Requirements - This is a list of requirements the new feature is to address.
This can be a reference to a previously created requirements document or a list of requirements that the design will address.
- Technical Solution - What is the design?
This should explain the design and how it works and fits into the OSGi architecture.
This is mainly for discussion within the project and is not necessarily the text that would go in the final specification.
But it is the starting point for that.
- Data Transfer Objects - DTOs are defined and used in many specifications.
Should this new design define any DTOs?

Once your _design/XXX_ branch has your commit with the new design document, along with any supporting API, code, etc., push this branch to your fork and make a pull request to the main repository.
This pull request will be used to confirm you have signed the [ECA](#legal-considerations) and will be used as the basis for creating the _design/XXX_ branch in the main repository.
A project committer must [create the new _design/XXX_ branch](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-and-deleting-branches-within-your-repository#creating-a-branch) in the main repository and then [change the base branch of the pull request](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/changing-the-base-branch-of-a-pull-request) to the newly created _design/XXX_ branch in the main repository.
At this point, the pull request can be merged into the main repository.

Discussion on the design document can take place in the created issue and updates to the design document and any supporting code can be made via additional pull requests against the _design/XXX_ branch.

To successfully conclude the design discussion, the project committers must call a vote of the project committers via an email to the `osgi-dev@eclipse.org` mail list.
The vote must remain open for at least 72 hours.
To succeed, the vote must have at least 3 votes with more `+1` than `-1`.
Any `-1` vote must be accompanied by an explanation of the vote.

If the design discussion successfully concludes, the _design/XXX_ branch is merged into the `main` branch, the tracking issue _XXX_ is closed, and work can then proceed to specification writing and API and TCK development as well as coordinating the development of a compatible implementation.

### Create issues

Any significant improvement should be documented as [a GitHub issue](https://github.com/osgi/org.osgi.service.log/issues) before anybody starts working on it.

Please take a moment to check that an issue doesn't already exist documenting your bug report or improvement proposal.
If it does, it never hurts to add a quick 👍 reaction.
This will help prioritize the most common problems and requests.

### Pull requests are always welcome

We are always thrilled to receive pull requests, and do our best to process them as fast as possible.

If your pull request is not accepted on the first try, don't be discouraged!
If there's a problem with the implementation, hopefully you received feedback on what to improve.

### Conventions

Fork the repository and make changes on your fork in a feature branch:

- If it's a bug fix branch, name it _issues/XXX_ where XXX is the number of the issue.
- If it's a requirements branch, name it _requirements/XXX_ where XXX is the number of the requirements tracking issue.
- If it's a design branch, name it _design/XXX_ where XXX is the number of the design tracking issue.

Write clean code.
Universally formatted code promotes ease of writing, reading, and maintenance.
The basic formatting rules are in `.editorconfig`: tabs for indentation, LF line endings, UTF-8, a final newline and no trailing whitespace.
Most editors, Eclipse included, apply it automatically or with an [EditorConfig](https://editorconfig.org/) plugin.
Make sure to avoid unnecessary white space changes which complicate diffs and make reviewing pull requests much more time consuming.

Pull requests descriptions should be as clear as possible and include a reference to all the issues that they address.

Pull requests must not contain commits from other users or branches.

Commit messages must start with a short summary (max. 50 chars) written in the imperative, followed by an optional, more detailed explanatory text which is separated from the summary by an empty line.

```
index: Remove absolute URLs from the OBR index

The url for the root was missing a trailing slash. Using File.toURI to
create an acceptable url.
```

Code review comments may be added to your pull request.
Discuss, then make the suggested modifications and push the amended commits to your feature branch.
Be sure to post a comment to the pull request after pushing.
The new commits will show up in the pull request automatically, but the reviewers may not be notified unless you comment.

Before the pull request is merged, make sure that you squash your commits into logical units of work using `git rebase -i` and `git push --force`.
After every commit, the test suite should be passing.
Include documentation changes in the same commit so that a revert would remove all traces of the feature or fix.

Commits that fix or close an issue should include a reference like `Closes #XXX` or `Fixes #XXX`, which will automatically close the issue when merged.

### Sign your work

Sign off on your commit in the commit comment footer.
By doing this, you assert original authorship of the commit and that you are permitted to contribute it.
This can be automatically added to your commit by passing `-s` to `git commit`, or by hand adding the following line to the footer of the commit.

```
Signed-off-by: Full Name <email>
```

Remember, if a blank line is found anywhere after the `Signed-off-by` line, the `Signed-off-by:` will be considered outside of the footer, and will fail the automated Signed-off-by validation.

It is important that you read and understand the legal considerations found below when signing off or contributing any commit.

### Merge approval

The maintainers will review your pull request and, if approved, will merge into the main repository.

If your pull request was originally a draft, don't forget to remove the draft status to signal to the maintainers that it is ready for review.

## Legal considerations

Please read the Eclipse Foundation guidance for [contributors](https://www.eclipse.org/projects/handbook/#contributing-contributors) in the Eclipse Foundation Project Handbook.

Your contribution cannot be accepted unless you are covered by the [ECA - Eclipse Foundation Contributor Agreement](https://www.eclipse.org/legal/ECA.php).

Here is the checklist for contributions to be _acceptable_:

1. [Create an account at Eclipse](https://accounts.eclipse.org/user/register).
2. Add your GitHub user name in your account settings.
3. Sign the [ECA](https://accounts.eclipse.org/user/eca) in your Eclipse account.
4. Ensure that you [_sign-off_](https://www.eclipse.org/projects/handbook/#resources-commit) your Git commits.
5. Ensure that you use the _same_ email address as your Eclipse account as the author of your commits.
6. Include the appropriate copyright notice and license at the top of each file.

The `eclipsefdn/eca` check on every pull request verifies that the author email address of each commit belongs to an Eclipse account that is covered by the ECA.
**Note**: Use the email address of your Eclipse account exactly as registered.

### Copyright Notice and Licensing Requirements

**It is the responsibility of each contributor to obtain legal advice, and to ensure that their contributions fulfill the legal requirements of their organization. This document is not legal advice.**

OSGi is licensed under the the Apache License, Version 2.0.
Any previously unlicensed contribution should be released under the same license.

- If you wish to contribute code under a different license, you must consult with a committer before contributing.
- For any scenario not covered by this document, please discuss the copyright notice and licensing requirements with a committer before contributing.

The template for the copyright notice and license is as follows:

```java
/*
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
```
