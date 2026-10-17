# MC Language Tools

Tools to compare Minecraft language (lang) files. It works from a graphical
window on Windows, or from the command line on any system with Java.

## Requirements

- Java 21 or newer (to run the `.jar`).
- Windows for the graphical window. The commands work on any system.

## Tasks

| Task | What it does |
|---|---|
| `missing` | Writes the keys that are in the source file but not in the target. |
| `prune` | Writes the target without the keys the source no longer has. Existing translations are kept. |

Typical use: `source` is the reference file (`en_us.json`) and `target` is a
translation (`es_mx.json`).

## Gui (Windows)

Run the `.jar` with no arguments, or double-click it:

```
java -jar MCLanguageTools-1.0.0.jar
```

1. Pick the task: **Missing** or **Prune**.
2. Use **Browse** to choose the **Source** and **Target** files and where to
   save the **Result**.
3. Set the options (see below) and press **Start**.
4. The summary, or the error, appears in the box at the bottom.

**Start** stays disabled until the source, the target and a result path are set.
With **In place** checked, the result path is not needed.

On other systems, running with no arguments prints an error and the usage, and
exits with code 1.

## Command line

Run the `.jar` followed by a task:

```
java -jar MCLanguageTools-1.0.0.jar <task> <source> <target> [output] [options]
```

### missing

```
java -jar MCLanguageTools-1.0.0.jar missing en_us.json es_mx.json missing.json
java -jar MCLanguageTools-1.0.0.jar missing en_us.json es_mx.json missing.json --empty-values
```

| Option | Effect |
|---|---|
| `--empty-values` | Write empty strings instead of the source values. |

### prune

```
java -jar MCLanguageTools-1.0.0.jar prune en_us.json es_mx.json es_mx_pruned.json
java -jar MCLanguageTools-1.0.0.jar prune en_us.json es_mx.json es_mx_pruned.json --source-order
java -jar MCLanguageTools-1.0.0.jar prune en_us.json es_mx.json --in-place
```

| Option | Effect |
|---|---|
| `--source-order` | Order the output by the source instead of the target order. |
| `--in-place` | Overwrite the target instead of writing a new file. Takes no output file. |

For `prune`, give either an output file or `--in-place`, never both.