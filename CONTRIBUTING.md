# Contributing to VoltCheck

Thank you for your interest in contributing to VoltCheck.

VoltCheck is an open-source Android application for monitoring battery charging, current, voltage, temperature, and battery health. This project is open for contributions from anyone who wants to help improve the application, documentation, UI/UX, performance, or overall project quality.

## Ways to Contribute

You can contribute to VoltCheck in several ways:

- Fixing bugs
- Improving documentation
- Adding new features
- Improving UI/UX
- Optimizing performance
- Reporting issues
- Suggesting feature improvements
- Improving code readability
- Testing the application on different Android devices

## Before You Start

Before making changes, please check the existing Issues and Pull Requests to avoid working on the same problem as someone else.

If you want to work on a major feature or large change, please open an Issue first to discuss the idea.

For small fixes, documentation updates, or minor improvements, you can directly create a Pull Request.

## Contribution Workflow

Follow these steps to contribute:

1. Fork this repository

2. Clone your forked repository

   ```bash
   git clone https://github.com/your-username/VoltCheck.git
   cd VoltCheck
   ```

3. Create a new branch

   ```bash
   git checkout -b feature/your-feature-name
   ```

4. Make your changes

5. Build and test the project

   ```bash
   ./gradlew assembleDebug
   ```

6. Commit your changes

   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

7. Push your branch to your forked repository

   ```bash
   git push origin feature/your-feature-name
   ```

8. Open a Pull Request to the `main` branch of this repository

## Branch Naming

Use clear and descriptive branch names.

Examples:

```bash
feature/add-battery-history
feature/add-export-feature
fix/charging-status-bug
fix/app-crash-on-startup
docs/update-readme
docs/improve-contributing-guide
ui/improve-dashboard-layout
refactor/clean-battery-service
```

Recommended branch prefixes:

| Prefix | Usage |
|---|---|
| `feature/` | For new features |
| `fix/` | For bug fixes |
| `docs/` | For documentation changes |
| `ui/` | For interface or layout improvements |
| `refactor/` | For code cleanup without changing behavior |
| `test/` | For testing-related changes |

## Commit Message Format

Use clear commit messages so the project history is easy to understand.

Recommended format:

```bash
type: short description
```

Examples:

```bash
feat: add battery history screen
fix: correct charging current calculation
docs: update installation guide
ui: improve main dashboard layout
refactor: simplify battery monitoring service
test: add basic battery status test
```

Common commit types:

| Type | Meaning |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation change |
| `ui` | UI or layout change |
| `refactor` | Code cleanup or restructuring |
| `test` | Testing-related change |
| `chore` | Maintenance or configuration change |

## Pull Request Guidelines

Before submitting a Pull Request, please make sure:

- The project builds successfully
- The application can run properly
- The code follows the existing project style
- The change is focused and not mixed with unrelated changes
- The Pull Request description clearly explains what was changed
- Documentation is updated if needed
- Screenshots are included for UI changes
- The branch is updated with the latest version of `main`

A good Pull Request description should include:

```md
## Summary
Explain briefly what you changed.

## Changes
- List the main changes
- Mention important files or features affected

## Testing
Explain how you tested the changes.

## Screenshots
Add screenshots if the change affects the UI.
```

## Reporting Bugs

When reporting a bug, please include as much information as possible.

Use this format:

```md
## Bug Description
Explain the bug clearly.

## Steps to Reproduce
1. Open the app
2. Go to ...
3. Tap ...
4. See the error

## Expected Result
Explain what should happen.

## Actual Result
Explain what actually happens.

## Device Information
- Device model:
- Android version:
- App version or commit:
- Installation method:

## Additional Information
Add screenshots, screen recordings, or logcat output if available.
```

## Suggesting Features

Feature suggestions are welcome.

When suggesting a feature, please explain:

- What problem the feature solves
- Why the feature is useful
- How the feature should work
- Whether the feature affects UI, background service, notification, export data, or battery monitoring

Use this format:

```md
## Feature Request
Explain the feature you want to suggest.

## Problem
Explain the problem or limitation.

## Proposed Solution
Explain how the feature should work.

## Additional Notes
Add examples, screenshots, or references if needed.
```

## Code Style

Please try to follow the existing code style in this project.

General guidelines:

- Use clear and meaningful variable names
- Keep functions focused and not too long
- Avoid unnecessary comments
- Add comments only when the logic is not obvious
- Keep formatting consistent with the existing code
- Do not add unrelated changes in the same Pull Request

## Android Development Guidelines

Because VoltCheck is an Android application, please make sure that your changes are suitable for Android development.

Guidelines:

- Test the app on an emulator or real Android device if possible
- Make sure battery monitoring features still work correctly
- Do not request unnecessary permissions
- Do not add background services without clear purpose
- Avoid changes that may increase battery usage without reason
- Keep UI readable on different screen sizes
- Make sure notification-related changes work properly

## Documentation Guidelines

Documentation improvements are very welcome.

You can help by improving:

- README
- Installation instructions
- Build instructions
- Feature descriptions
- Screenshots
- Code comments
- Contribution guide

When editing documentation:

- Use clear and simple English
- Keep formatting consistent
- Make sure links and image paths work
- Avoid adding outdated or untested information

## UI/UX Guidelines

If you make UI changes, please include screenshots in your Pull Request.

For UI improvements, please consider:

- Readability
- Spacing
- Consistent layout
- Clear icons and labels
- Simple navigation
- Light and dark mode compatibility if available
- Usability on different Android screen sizes

## What to Avoid

Please avoid:

- Uploading build files or generated files
- Uploading local configuration files
- Uploading sensitive data
- Making unrelated changes in one Pull Request
- Changing project structure without discussion
- Adding large dependencies without a clear reason
- Submitting code that has not been tested
- Removing existing features without explanation

## Files That Should Not Be Committed

Please do not commit files such as:

```txt
local.properties
.gradle/
build/
app/build/
.idea/
*.iml
.DS_Store
```

Make sure your `.gitignore` is working properly before submitting a Pull Request.

## Review Process

After you submit a Pull Request:

1. The maintainer will review your changes
2. The maintainer may ask for revisions
3. You can update your branch with the requested changes
4. Once approved, the Pull Request may be merged into the main repository

Please be patient during the review process.

## License

By contributing to VoltCheck, you agree that your contributions will be licensed under the same license as this project.

VoltCheck is licensed under the MIT License. See the `LICENSE` file for more details.

## Thank You

Thank you for helping improve VoltCheck.

Every contribution is appreciated, whether it is code, documentation, bug reports, suggestions, or testing feedback.
