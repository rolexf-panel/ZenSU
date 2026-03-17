# AI Agent Instructions for ZenSU

## Project Overview

ZenSU is a highly customizable and user-friendly root manager for Android that supports multiple root solutions (KernelSU, KernelSU-Next, Magisk, APatch) with module management capabilities.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Min SDK**: 30 (Android 11)
- **Target SDK**: 35 (Android 15)
- **Architecture**: MVVM with Clean Architecture
- **Key Libraries**:
  - Jetpack Compose BOM
  - Navigation Compose
  - Hilt for DI
  - Kotlin Coroutines + Flow
  - libsu (topjohnwu) for shell execution

## Code Style

1. **Language**: Bahasa Indonesia for comments and documentation
2. **Naming**: English for all code identifiers (variables, functions, classes)
3. **Package Structure**: 
   ```
   com.zensu
   ├── ui/           # Compose UI (screens, components, theme)
   ├── viewmodel/    # MVVM ViewModels  
   ├── repository/   # Data layer
   ├── model/        # Data models
   ├── detector/     # Root solution detection
   └── util/         # Utilities
   ```

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean
./gradlew clean
```

## Key Features Implementation

### 1. Root Detection
Detect installed root solution by checking:
- KernelSU: `/data/adb/ksu/`
- KernelSU-Next: `/data/adb/ksun/`
- Magisk: `/sbin/magisk`
- APatch: `/data/adb/apatch/`

### 2. Module Types Supported
- Magisk Modules (`.zip` with `module.prop`)
- KPM - Kernel Patch Modules (`.kpm` files)
- APM - APatch Modules
- KernelSU Modules (with WebUI support)

### 3. Repository System
- Built-in: KernelSU Modules Repo
- Support custom repositories (MMRL JSON format)
- Module update checking

## Development Guidelines

1. Always use Kotlin Coroutines for async operations
2. Follow Material Design 3 guidelines
3. Implement Simple Mode (default) and Expert Mode toggle
4. Handle edge cases gracefully with user-friendly error messages
5. Test on multiple root solutions

## Important Files

- `app/build.gradle.kts` - App module configuration
- `gradle.properties` - Gradle properties including SDK versions
- `app/src/main/AndroidManifest.xml` - App manifest

## Versioning

Follow semantic versioning. Update version in:
- `app/build.gradle.kts` (versionCode, versionName)
- GitHub release tag
