---
name: start-changeset
description: >-
  Pick up a changeset and prepare the local workspace. Use when the user
  provides a changeset ID (UUID or short ID), asks to start working on a
  changeset, pick up a changeset, resume work on a changeset, or mentions a
  changeset by name. Also use when the user pastes what looks like a UUID or
  short hex identifier in the context of starting work.
---

# Start Changeset

Workflow to pick up an in-progress changeset and prepare the local workspace.

## Conventions

- **Branch naming**: `intent/<changeset-id>-<slugified-changeset-name>`
- **Changeset file**: `.intent/changesets/<short-id>-<slug>.md`
- **Specs**: `.intent/specs/<id>.md` (linked from changeset file)

### Repo discovery

Do **not** use a hardcoded repo list. The user splits repos across different workspaces, so only operate on repos that are actually present in the **current** workspace.

Determine which repos are available by checking the workspace paths provided in the session context (the `Workspace Paths` list). The directory name maps 1:1 to the repo name (e.g. workspace path `.../intent-frontend` → repo `intent-frontend`).

Only fetch, checkout, and inspect repos that appear in the current workspace. Silently skip all others.

## Workflow

### Step 1: Discover changeset branches

For each repo in the **current workspace**, fetch and list remote branches matching the `intent/` prefix:

```bash
git fetch origin
git branch -r --list "origin/intent/*"
```

Collect all unique changeset branches across repos. Extract the changeset short ID (first 8 chars after `intent/`) and the human-readable slug from the branch name.

Deduplicate by changeset ID (the same changeset may appear in multiple repos).

### Step 2: Ask user to pick one

If the user already provided a changeset ID or name, match it against the discovered branches. Otherwise, present the list using AskQuestion. Show the slug (converted back to readable title) for each option.

### Step 3: Checkout and pull in all repos

For each repo in the current workspace that has the selected changeset branch:

```bash
git checkout intent/<changeset-id>-<slug>
git pull
```

Skip repos that don't have a matching branch for this changeset.

### Step 4: Read changeset description and specs

After checkout, read the changeset file from `.intent/changesets/` in any of the checked-out repos (the file is identical across repos sharing the same changeset).

Find the changeset file by matching the short ID prefix:

```bash
ls .intent/changesets/<short-id>-*
```

Read the changeset markdown file. Then read **all** spec files it references (linked as `../specs/<id>.md` in the Specs section). Every linked spec must be read in full — they contain the detailed implementation requirements.

### Step 5: Read diff against default branch

Determine the default branch:

```bash
git symbolic-ref refs/remotes/origin/HEAD | sed 's@^refs/remotes/origin/@@'
```

For each checked-out repo, show what has changed:

```bash
git diff origin/<default-branch> --stat
```

This gives context on work already done on the branch.

### Step 6: Outline implementation

Present a **brief** implementation outline in chat covering:
- What each affected repo needs to do
- Key files likely involved (infer from spec + repo structure)
- Suggested order of implementation
- Open questions or ambiguities

Keep it to ~10-20 bullet points max. Do not start implementing yet.
