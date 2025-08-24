# Suggested Commands for Dingo Development

## Essential Development Commands

### Build & Development
```bash
./gradlew build                    # Complete build with tests
./gradlew assemble                 # Build without tests
./gradlew clean                    # Clean all build artifacts
```

### Testing
```bash
./gradlew test                     # Run all unit tests
./gradlew testDebugUnitTest        # Run debug unit tests
./gradlew connectedAndroidTest     # Run instrumentation tests
./gradlew check                    # Run all checks (lint, tests, etc.)
```

### Code Quality
```bash
./gradlew ktlintFormat             # Auto-format code (run before committing)
./gradlew ktlintCheck              # Check code formatting
./gradlew lint                     # Run Android lint
./gradlew lintFix                  # Apply safe lint fixes
```

### Pre-commit Workflow
```bash
./gradlew clean ktlintFormat build  # Recommended before committing
```

## Windows-Specific Commands

Since the project runs on Windows, use these system commands:

### File Operations
```cmd
dir                               # List directory contents (equivalent to ls)
cd <directory>                    # Change directory
copy <source> <destination>       # Copy files
del <file>                        # Delete files
type <file>                       # Display file contents (equivalent to cat)
```

### Git Operations
```bash
git status                        # Check repository status
git add .                         # Stage all changes
git commit -m "message"           # Commit changes
git push                          # Push to remote
git pull                          # Pull from remote
```

### Search Commands
```cmd
findstr "pattern" *.kt            # Search for pattern in Kotlin files
dir /s *.kt                       # Find all Kotlin files recursively
```

## Gradle Wrapper
Always use `./gradlew` (or `gradlew.bat` on Windows) instead of global Gradle installation to ensure consistent build environment.

## Pre-commit Hook
The project has an automated git pre-commit hook that runs:
1. `ktlintFormat` - Auto-format code
2. `check` - Run all checks and tests
3. `ktlintCheck` - Verify formatting

This ensures code quality before commits are allowed.