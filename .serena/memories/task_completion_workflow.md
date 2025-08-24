# Task Completion Workflow

## Required Steps When Completing Any Task

### 1. Code Quality Checks (MANDATORY)
Always run these commands before committing or considering a task complete:

```bash
./gradlew ktlintFormat             # Auto-format code
./gradlew check                    # Run all checks, lint, and tests
./gradlew ktlintCheck              # Verify code formatting
```

### 2. Pre-commit Workflow
The recommended workflow before any commit:
```bash
./gradlew clean ktlintFormat build
```

### 3. Automated Pre-commit Hook
The project has a git pre-commit hook that automatically runs:
1. `ktlintFormat` - Formats code
2. `check` - Runs all checks and tests  
3. `ktlintCheck` - Verifies formatting

This hook will **prevent commits** if any step fails.

### 4. Testing Requirements
- **Unit tests**: Must pass (`./gradlew test`)
- **Integration tests**: Must pass (`./gradlew connectedAndroidTest`)
- **All checks**: Must pass (`./gradlew check`)

### 5. Build Verification
Ensure the project builds successfully:
```bash
./gradlew build                    # Complete build with tests
```

### 6. Commit Guidelines
Follow conventional commit format as specified in CLAUDE.md:

```
Title (conventional commit format):
Start with feat(<component/module>): then a concise summary

✨ Features (bullet list):
List new features or visible behaviors

🔧 Technical Changes (bullet list):  
List code-level enhancements

⚠️ Drawbacks or known issues (optional):
Mention any known quirks

✅ Reference (optional):
Add internal references if available
```

## Error Resolution
If any quality check fails:

1. **KtLint errors**: Run `./gradlew ktlintFormat` to auto-fix
2. **Build errors**: Check error reports in `build/reports/`
3. **Test failures**: Fix failing tests before proceeding
4. **Lint issues**: Use `./gradlew lintFix` for safe auto-fixes

## Module-Specific Considerations
- **:domain**: Ensure no dependencies on other modules
- **:data**: Verify Firebase integration and repository implementations
- **:ui**: Test Compose components and ViewModels
- **:app**: Ensure proper DI setup and navigation
- **:common**: Verify shared components work across modules

## Definition of "Task Complete"
A task is only complete when:
- [ ] All code quality checks pass
- [ ] All tests pass
- [ ] Code builds successfully
- [ ] Pre-commit hook would pass
- [ ] Code follows project conventions
- [ ] No breaking changes to existing functionality (unless intended)
- [ ] Documentation updated if needed