Usage for git service

```bash
val service = RobustGitDiffService(repoPath)
val changedFiles = service.getChangedFilesWithDiffs()
for ((key, value) in changedFiles) {
    println("Key: $key, Value: $value")
```