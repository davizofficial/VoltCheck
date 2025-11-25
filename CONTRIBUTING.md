# Contributing to VoltCheck

Thank you for your interest in contributing to VoltCheck! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

By participating in this project, you agree to maintain a respectful and collaborative environment for all contributors.

## How to Contribute

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When creating a bug report, include:

- Clear and descriptive title
- Detailed description of the issue
- Steps to reproduce the behavior
- Expected behavior
- Screenshots (if applicable)
- Device information (model, Android version)
- App version

### Suggesting Enhancements

Enhancement suggestions are welcome. Please provide:

- Clear and descriptive title
- Detailed description of the proposed feature
- Explanation of why this enhancement would be useful
- Possible implementation approach (optional)

### Pull Requests

1. Fork the repository
2. Create a new branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Make your changes following the coding standards
4. Test your changes thoroughly
5. Commit your changes with clear messages:
   ```bash
   git commit -m "Add: Brief description of changes"
   ```
6. Push to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
7. Open a Pull Request with a detailed description

## Development Setup

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 8 or higher
- Android SDK with API 24+
- Gradle 7.0+

### Setup Instructions

1. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/VoltCheck.git
   cd VoltCheck
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build the project:
   ```bash
   ./gradlew build
   ```

## Coding Standards

### Java Style Guide

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Keep methods focused and concise
- Add comments for complex logic
- Use proper indentation (4 spaces)

### Code Organization

- Place new features in appropriate packages
- Keep related functionality together
- Maintain separation of concerns
- Follow MVVM architecture pattern

### XML Resources

- Use descriptive resource names
- Follow naming conventions:
  - Layouts: `activity_name.xml`, `fragment_name.xml`
  - Drawables: `ic_name.xml`, `bg_name.xml`
  - IDs: `camelCase` (e.g., `btnSubmit`, `tvTitle`)
- Keep XML files properly formatted

### Commit Messages

Use clear and descriptive commit messages:

- `Add: New feature or functionality`
- `Fix: Bug fix`
- `Update: Modification to existing feature`
- `Refactor: Code restructuring`
- `Docs: Documentation changes`
- `Style: Formatting, missing semicolons, etc.`
- `Test: Adding or updating tests`

Example:
```
Add: Battery wear level calculation feature

- Implement wear level algorithm
- Add UI display for wear percentage
- Update settings to include wear tracking option
```

## Testing

- Test your changes on multiple Android versions (if possible)
- Verify that existing functionality still works
- Test both light and dark modes
- Test with different language settings
- Ensure no memory leaks or performance issues

## Documentation

- Update README.md if adding new features
- Update CHANGELOG.md with your changes
- Add inline comments for complex code
- Update relevant documentation files

## Review Process

1. All submissions require review
2. Maintainers will review your pull request
3. Address any requested changes
4. Once approved, your contribution will be merged

## Questions?

If you have questions about contributing, feel free to:

- Open an issue with the `question` label
- Start a discussion in GitHub Discussions

## License

By contributing to VoltCheck, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to VoltCheck!
