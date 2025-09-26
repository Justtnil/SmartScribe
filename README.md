# 🤖 SmartScribe — AI-Powered Voice Notes with Local Privacy


SmartScribe transforms spoken words into intelligent, summarized notes using on-device AI. No cloud processing, no data collection — just pure privacy with powerful local intelligence.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6+-pink?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Room-2.6+-green?logo=sqlite&logoColor=white)](https://developer.android.com/jetpack/androidx/releases/room)
[![License](https://img.shields.io/badge/License-MIT-purple)](LICENSE)

## 🌟 Features

### 🎙️ Intelligent Voice Processing
- **Real-time speech-to-text** with live transcription
- **AI-powered summarization** that categorizes your notes automatically
- **Smart content detection** for meetings, lectures, shopping lists, and ideas

### 📱 Modern Material 3 UI
- **Dark/Light/System theme toggle** with sun/moon icons
- **Floating action button** for quick note creation
- **Searchable notes list** with category filtering
- **Haptic feedback** for premium tactile experience
- **Smooth animations** and transitions throughout

### 🔒 Privacy-First Architecture
- **100% offline processing** — no internet required after setup
- **Local database storage** using Room persistence library
- **No data collection** — your notes stay on your device
- **Microphone permission only** — minimal permissions required

### 📝 Note Management
- **Automatic categorization** (Meeting, Lecture, Shopping, Ideas, General)
- **Smart title generation** from your content
- **Export notes** as text or PDF
- **Edit and delete** notes with confirmation haptics

## 📸 Screenshots

| Notes List | Voice Input | Note Detail |
|------------|-------------|-------------|
| ![Notes List](https://via.placeholder.com/300x600/121212/FFFFFF?text=Notes+List+%7C+Theme+Toggle+%7C+Search) | ![Voice Input](https://via.placeholder.com/300x600/6200EE/FFFFFF?text=Voice+Input+%7C+AI+Summary+%7C+Bottom+Nav) | ![Note Detail](https://via.placeholder.com/300x600/1F1F1F/FFFFFF?text=Note+Detail+%7C+Edit+%7C+Share) |

## 🚀 Getting Started

### Requirements
- **Android 13+** (API 33+)
- **Kotlin 1.9+**
- **Android Studio Flamingo+**

### Installation
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/smartscribe.git
   ```
2. Open in Android Studio
3. Build and run on your device

### Permissions
- **Microphone** (`RECORD_AUDIO`) - Required for voice input
- **Storage** - For PDF export (handled automatically by Android)

## 🛠️ Tech Stack

### Architecture
- **MVVM Pattern** with Clean Architecture principles
- **Jetpack Compose** for modern declarative UI
- **Room Database** for local persistence
- **Kotlin Coroutines** for asynchronous operations
- **Navigation Component** for type-safe navigation

### Key Libraries
```kotlin
// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// Compose
implementation("androidx.activity:activity-compose:1.8.2")
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Database
implementation("androidx.room:room-runtime:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.6")
```

## 🎨 UI/UX Highlights

### Theme System
- **Three-way theme toggle**: System / Light / Dark
- **Dynamic color support** (Android 12+)
- **Automatic status bar color** matching theme

### Haptic Feedback
- **Context-aware vibrations** for different interactions:
  - **Medium tap** for primary actions (FAB press)
  - **Light tap** for secondary actions (search focus)
  - **Success pattern** for successful saves
  - **Error pulse** for failed operations
  - **Selection tap** for navigation

### Animations
- **Smooth AI summary fade-in** with vertical expansion
- **Scale animations** on button presses
- **Animated visibility** for status messages

## 📱 User Flow

1. **Open app** → Land on "My Notes" screen
2. **Tap "+" FAB** → Navigate to voice input screen
3. **Speak or type** → AI automatically generates summary
4. **Tap Save** → Note saved with smart categorization
5. **Return to notes list** → View, search, and filter notes
6. **Tap any note** → View details, edit, or share

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgements

- **Jetpack Compose** team for the amazing UI toolkit
- **Android Room** team for robust local database solution
- **Material Design** team for beautiful design guidelines
- **Open source community** for inspiration and support

---

> **SmartScribe** — Where your voice becomes intelligent notes, privately and securely on your device. 🎙️➡️📝

[![Star on GitHub](https://img.shields.io/github/stars/Justtnil/SmartScribe?style=social)](https://github.com/Justtnil/SmartScribe)
