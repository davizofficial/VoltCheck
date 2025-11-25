# VoltCheck

A comprehensive battery monitoring application for Android devices.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue.svg)](https://developer.android.com/about/versions/nougat)
[![License](https://img.shields.io/badge/License-Open%20Source-brightgreen.svg)](LICENSE)

## Overview

VoltCheck is an advanced battery monitoring application designed for Android devices. It provides real-time battery statistics, health monitoring, and comprehensive charging analysis. Built with modern Material Design principles, the application offers an intuitive and professional interface for tracking device battery performance.

## Features

### Core Monitoring
- Real-time current measurement (mA/Ampere)
- Voltage tracking with high precision
- Temperature monitoring
- Battery level percentage
- Battery health status
- Capacity information display
- Min/Max current tracking

### Advanced Capabilities
- Time to full charge estimation
- Historical data logging
- CSV data export functionality
- Background monitoring service
- Customizable refresh intervals (0.5s - 5s)
- Design capacity configuration

### Notification System
- Battery full alerts
- Low battery/slow charging warnings
- Fast charging detection
- High temperature alerts
- Fully customizable notification preferences

### Customization Options
- Dark mode support
- Multi-language support (English, Indonesian)
- Unit selection (mA/Ampere)
- Decimal precision toggle
- Custom battery level alarms
- Configurable alarm thresholds

### Data Management
- Export battery data to CSV format
- Share exported data
- Clear history functionality
- Session summary tracking

## Technical Specifications

### Technology Stack
- **Language**: Java
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 13 (API 33)
- **Build System**: Gradle
- **UI Framework**: Material Design Components
- **Charting Library**: MPAndroidChart v3.1.0
- **Data Persistence**: SharedPreferences, File System

### Architecture
- MVVM (Model-View-ViewModel) pattern
- Service-based background monitoring
- Modular component design
- Efficient data logging system

### Key Components
```
MainActivity          - Primary battery monitoring interface
SettingsActivity      - Comprehensive settings management
BatteryService        - Background monitoring service
BatteryDataLogger     - Data logging and export system
LocaleHelper          - Multi-language support handler
NotificationUtil      - Notification management system
```

## System Requirements

- Android 7.0 (Nougat) or higher
- Device with battery current measurement support
- Storage permission for data export functionality
- Approximately 10MB of storage space

## Installation

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/davizofficial/VoltCheck.git
cd VoltCheck
```

2. Open the project in Android Studio (Arctic Fox or newer recommended)

3. Build the project:
```bash
./gradlew assembleDebug
```

4. Install on device:
```bash
./gradlew installDebug
```

### From Release

Download the latest APK from the [Releases](https://github.com/davizofficial/VoltCheck/releases) page and install on your Android device.

## Usage Guide

### Initial Setup
1. Launch the application to view real-time battery statistics
2. Grant necessary permissions when prompted
3. Navigate to Settings to customize preferences

### Monitoring
- Main screen displays current battery metrics
- Swipe or scroll to view detailed information
- Min/Max values are tracked automatically

### Configuration
1. Tap the Settings icon in the top-right corner
2. Configure refresh interval, units, and display preferences
3. Enable background service for continuous monitoring
4. Set custom alarms for specific battery levels

### Data Export
1. Navigate to Settings > Data & Permissions
2. Tap "Export Data"
3. Choose to share or save the CSV file
4. Optionally clear data after export

## Project Structure

```
VoltCheck/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/voltcheck/app/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SettingsActivity.java
│   │   │   │   ├── BatteryService.java
│   │   │   │   └── utils/
│   │   │   │       ├── BatteryDataLogger.java
│   │   │   │       ├── LocaleHelper.java
│   │   │   │       └── NotificationUtil.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── values/
│   │   │   │   └── values-night/
│   │   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

## Contributing

Contributions are welcome and appreciated. This project is open source and designed for educational purposes.

### How to Contribute

1. Fork the repository
2. Create a feature branch:
   ```bash
   git checkout -b feature/YourFeatureName
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add: Brief description of your changes"
   ```
4. Push to your branch:
   ```bash
   git push origin feature/YourFeatureName
   ```
5. Open a Pull Request with a detailed description

### Contribution Guidelines
- Follow existing code style and conventions
- Write clear commit messages
- Test your changes thoroughly
- Update documentation as needed
- Ensure backward compatibility

## Development

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 8 or higher
- Android SDK with API 24+
- Gradle 7.0+

### Setup Development Environment
```bash
# Clone repository
git clone https://github.com/davizofficial/VoltCheck.git

# Open in Android Studio
# File > Open > Select VoltCheck directory

# Sync Gradle files
# Build > Make Project
```

## License

This project is provided as-is for educational and learning purposes. You are free to use, modify, and distribute this application. See the repository for detailed license information.

## Author

**davizofficial**

- GitHub: [@davizofficial](https://github.com/davizofficial)
- Project Link: [https://github.com/davizofficial/VoltCheck](https://github.com/davizofficial/VoltCheck)

## Acknowledgments

This project utilizes the following open-source libraries and resources:

- [Material Design Components](https://github.com/material-components/material-components-android) - Google
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Philipp Jahoda
- [Android Open Source Project](https://source.android.com/) - Google

## Support

For questions, bug reports, or feature requests:

- **Issues**: [GitHub Issues](https://github.com/davizofficial/VoltCheck/issues)
- **Pull Requests**: [GitHub Pull Requests](https://github.com/davizofficial/VoltCheck/pulls)
- **Discussions**: [GitHub Discussions](https://github.com/davizofficial/VoltCheck/discussions)

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed history of changes.

## Roadmap

Future enhancements under consideration:

- Widget support for home screen
- Battery wear level calculation
- Charging cycle counter
- Power consumption analysis
- Battery health predictions
- Cloud backup for historical data

---

**Note**: Battery current measurement accuracy depends on device hardware capabilities. Some devices may not support all features.

Copyright (c) 2025 davizofficial. All rights reserved.
