# ZenSU

A highly customizable and user-friendly root manager for Android.

## Features

- 🔐 **Multi-Root Support**: Works with KernelSU, KernelSU-Next, Magisk, and APatch
- 📦 **Module Management**: Install and manage Magisk modules, KPM (Kernel Patch Modules), and APM (APatch Modules)
- 🎨 **Highly Customizable**: User-friendly interface with Simple Mode and Expert Mode
- 🔄 **Triple Repository Support**: KernelSU Modules Repo, MMRL Community Repo, and custom user repositories
- 📱 **Modern Design**: Built with Jetpack Compose and Material Design 3

## Requirements

- Android 11+ (API 30+)
- One of the following root solutions:
  - KernelSU / KernelSU-Next
  - Magisk
  - APatch

## Installation

1. Install a root solution (KernelSU, Magisk, or APatch) on your device
2. Download the latest ZenSU APK from [Releases](https://github.com/rolexf-panel/ZenSU/releases)
3. Install the APK on your device
4. Grant root permissions when prompted

## Usage

### Simple Mode (Default)
Perfect for beginners. Shows essential features only:
- View root status
- Enable/disable modules
- Basic superuser management

### Expert Mode
Full control for advanced users:
- Complete module management
- KPM support
- Repository management
- Detailed logs
- Theme customization

Toggle between modes in Settings.

## Building from Source

```bash
# Clone the repository
git clone https://github.com/rolexf-panel/ZenSU.git
cd ZenSU

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## License

- Source code: GPL-3.0
- See [LICENSE](LICENSE) for details

## Credits

- [KernelSU](https://github.com/tiann/KernelSU) - Base root solution
- [KernelSU-Next](https://github.com/rifsxd/KernelSU-Next) - KernelSU development
- [MMRL](https://github.com/MMRLApp/MMRL) - Multi-root module manager reference
- [Magisk](https://github.com/topjohnwu/Magisk) - The magic mask for Android

## Support

- Issues: [GitHub Issues](https://github.com/rolexf-panel/ZenSU/issues)
- Discussions: [GitHub Discussions](https://github.com/rolexf-panel/ZenSU/discussions)

---

<p align="center">
Made with ❤️ by Bradar
</p>
