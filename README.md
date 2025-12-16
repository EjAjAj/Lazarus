# Lazarus
<img src="https://github.com/EjAjAj/Lazarus/blob/main/assets/Lazarus.png" alt="Lzarus Logo" width="200" />

A semantic code analysis and reporting plugin for IntelliJ IDEA that generates intelligent insights about your Java/Kotlin codebase using graph-based analysis and LLM-powered semantic understanding.

**Goal: optimize token consumption**

## Overview

Lazarus is an IntelliJ IDEA plugin that builds and maintains a semantic graph of your codebase, analyzing relationships between files, classes, methods, and variables. It leverages LLM agents to generate contextual reports about code structure, dependencies, and changes, making it easier to understand complex codebases and track semantic impacts of modifications.

### Key Features

- **Semantic Code Graph**: Automatically builds a comprehensive graph representation of your codebase, capturing:
  - File → Class → Method → Variable hierarchies
  - Method call relationships
  - Field access patterns
  - Class inheritance structures
<img src="https://github.com/EjAjAj/Lazarus/blob/main/assets/SemanticCodeGraph.png" alt="Semantic Code Graph"/>

- **Intelligent Reporting**: Three types of semantic reports:
  - **File Semantic Report** (`Ctrl+Alt+S`): Analyze a single file and its connections
  - **Global Semantic Report** (`Ctrl+Alt+Shift+S`): Generate a project-wide semantic analysis
  - **Git Diff Semantic Report** (`Ctrl+Alt+Shift+D`): Fetch remote changes and analyze their semantic impact

- **Git Integration**: Seamlessly tracks and analyzes changes across branches with semantic diff capabilities

- **LLM-Powered Analysis**: Integrates with external LLM services to generate human-readable insights about code structure and changes

## Architecture

### Core Components

1. **Graph Building** (`PsiGraphBuilder`)
   - Uses IntelliJ's PSI (Program Structure Interface) to parse source code
   - Builds an indexed code graph with nodes (files, classes, methods, variables) and edges (relationships)
   - Automatically initializes on project load and maintains incremental updates

2. **Graph Storage** (`SemGraphStorage`)
   - Persists the semantic graph to disk in the `.lazarus` directory
   - Supports efficient loading and saving of graph state

3. **Graph Analysis** (`SemGraphAnalysis`)
   - Analyzes graph structure to identify connected components
   - Traces relationships and generates connection descriptions
   - Supports both file-level and project-wide analysis

4. **LLM Agent Service** (`LLMAgentService`)
   - Communicates with external LLM API for semantic report generation
   - Formats graph analysis data with file contents for LLM processing
   - Supports incremental and general report types

5. **Git Services**
   - `DiffService`: Fetches and processes git diffs
   - `CommandRepository`: Executes git commands
   - `DiffRepository`: Manages diff data retrieval

### Data Model

**Graph Nodes:**
- `FILE`: Source file nodes
- `CLASS`: Class/interface declarations
- `METHOD`: Method/function definitions
- `VARIABLE`: Field declarations

**Graph Edges:**
- `CONTAINS`: Hierarchical containment (file → class, class → method)
- `CALLS`: Method invocation relationships
- `ACCESSES`: Field access patterns
- `INHERITS`: Class inheritance relationships

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd Lazarus
```

2. Build the plugin:
```bash
cd lazarus-plugin
./gradlew buildPlugin
```

3. Install in IntelliJ IDEA:
   - Go to `Settings` → `Plugins` → `⚙️` → `Install Plugin from Disk`
   - Select the built plugin from `lazarus-plugin/build/distributions/`

## Configuration

The plugin requires an external LLM service running. Configure the endpoint in `HttpClient.kt` (default: `http://localhost:8000`).

## Usage

### Quick Start

1. Open a Java/Kotlin project in IntelliJ IDEA
2. The plugin automatically builds the semantic graph on project load
3. Use keyboard shortcuts or toolbar actions to generate reports:

### Actions

| Action | Shortcut | Description |
|--------|----------|-------------|
| Generate Semantic Report | `Ctrl+Alt+S` | Analyze the currently open file |
| Global Semantic Report | `Ctrl+Alt+Shift+S` | Analyze entire project |
| Fetch-Compare Semantics | `Ctrl+Alt+Shift+D` | Fetch remote changes and show semantic diff |

### Example Workflow

1. **Analyze a file**: Right-click in editor → "Generate Semantic Report"
2. **View connections**: The report shows all related files, methods called, and semantic descriptions
3. **Track changes**: Before merging, use "Fetch-Compare Semantics" to understand the impact of remote changes

## Technical Details

- **Language**: Kotlin
- **Target Platform**: IntelliJ IDEA 2025.1.4+
- **Build System**: Gradle with IntelliJ Platform Plugin
- **Dependencies**:
  - JGit for Git operations
  - Kotlinx Serialization for JSON handling
  - SLF4J/Logback for logging

## Project Structure

```
Lazarus/
├── .lazarus/                    # Graph storage directory
└── lazarus-plugin/
    ├── src/main/kotlin/org/example/lazarusplugin/
    │   ├── Main.kt             # Plugin startup
    │   ├── actions/            # UI actions (reports, toolbar buttons)
    │   ├── models/             # Data models (graph, git)
    │   ├── repository/         # Git command/diff repositories
    │   ├── services/
    │   │   ├── git/           # Git integration services
    │   │   └── graph/         # Graph building, storage, analysis
    │   ├── ui/                # Report display dialogs
    │   └── utils/             # HTTP client, file utilities
    └── src/main/resources/
        └── META-INF/plugin.xml
```

## Development

### Requirements

- JDK 21
- Gradle 8.x
- IntelliJ IDEA 2025.1.4+

### Building

```bash
./gradlew build
```

### Testing

```bash
./gradlew test
```

### Running in Development

```bash
./gradlew runIde
```

## Limitations

- Currently supports Java and Kotlin files only
- Requires external LLM service for report generation
- Graph building may take time on large projects

## Contributing

Contributions are welcome! Please ensure:
- Code follows Kotlin conventions
- Tests are included for new features
- Plugin compatibility is maintained

## License

MIT Licencse 

## Authors

EjAj

---

**Note**: This plugin is under active development. Features and APIs may change.
