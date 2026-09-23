# org.osgi.service.log

OSGi Log Service Specification

Part of the [OSGi Specification Project](https://projects.eclipse.org/projects/technology.osgi).

## Compatible implementations

Implementations known to provide this specification. Additions and
corrections are welcome — please open a pull request.

| Project | Link | Notes |
|---------|------|-------|
| _none listed yet_ | | |

## Build

```
mvn clean verify
```

The `build` workflow builds every push and pull request the same way; every
push to `main` deploys a fresh SNAPSHOT to Sonatype Central snapshots, and
a weekly schedule keeps it fresh in quiet times.

## Versioning and releases

The version lives in exactly one place: the `-Drevision=` line in
[`.mvn/maven.config`](.mvn/maven.config). The poms only reference
`${revision}` — never add a second copy of the version anywhere else.

A release is the creation of a git tag whose name equals that version,
without any prefix: `1.2.3`, not `v1.2.3`.

1. Open a pull request that sets `-Drevision=1.2.3` in `.mvn/maven.config`
   and merge it.
2. Go to **Releases → Draft a new release**, type `1.2.3` under
   "Choose a tag" and select "Create new tag on publish". A saved draft
   does not create the tag yet, so a draft is a safe intermediate state.
3. Leave **Target: `main`** (the default) — the tag will point at the
   merge commit.
4. Click **Publish release**. Publishing creates the tag, and the tag
   triggers the release workflow.

The command-line equivalent is
`gh release create 1.2.3 --target main --generate-notes`.

The release workflow refuses a tag that does not exactly match
`.mvn/maven.config`, and refuses `-SNAPSHOT` versions. It builds and tests
the repository, then stages the signed artifacts to Maven Central — the
final publish stays a manual step in the Central portal.

## History

This repository was extracted from the OSGi monorepo with `git filter-repo`.
It carries the complete history of every file it contains, including the
history from before any rename inside the monorepo.

A single commit moves the files into the Maven layout (`src` →
`src/main/java`, TCK projects under `tck/`, specification chapters under
`spec/`). Since Git follows paths rather than files, `git log <file>` shows
only that one commit. To see the whole story:

```
git log --follow <file>
```

`git blame` follows the renames on its own. The unfiltered monorepo remains
archived at [osgi/osgi](https://github.com/osgi/osgi/).

## Contact

- Project contact page: <https://projects.eclipse.org/projects/technology.osgi/contact>
- Mailing lists:
  - [osgi-dev](https://accounts.eclipse.org/mailing-list/osgi-dev) —
    developer list for discussion of the OSGi Specification Project
  - [osgi-users](https://accounts.eclipse.org/mailing-list/osgi-users) —
    public list for discussion of OSGi technology and specifications; ask
    your technical questions about OSGi here
  - [osgi-wg](https://accounts.eclipse.org/mailing-list/osgi-wg) —
    OSGi Working Group mail list
- Slack: <https://osgiwg.slack.com/>
- Spec call: Zoom call every **second Wednesday** — see the
  [calendar](https://calendar.google.com/calendar/u/0/newembed?src=c_fh3lhb5p0l29f6phu2ndifh4a4@group.calendar.google.com)
  for the exact time (please mind your local timezone).
