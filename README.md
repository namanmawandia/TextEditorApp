# Jetpack Compose Rich Text Editor

A high-performance, custom Rich Text Editor for Android built using **Jetpack Compose** and the **Android Spannable API**. 

This project demonstrates a sophisticated bridge between modern Compose UI and legacy `EditText` capabilities to achieve real-time text formatting that native `TextField` currently struggles to handle.

## 🚀 The Challenge
Handling "Typing Modes" in text editors is notoriously difficult. If a user toggles **Bold** on an empty selection, they expect the *next* characters typed to be bold. This project implements a robust "capture and apply" logic that manages these spans dynamically without re-rendering the entire text buffer, ensuring smooth performance even with large documents.

## ✨ Key Features

- **Dynamic Typing Modes**: Toggle Bold, Italic, Underline, and Strikethrough. The editor intelligently applies these styles to newly typed text.
- **Color Customization**: Full support for `ForegroundColorSpan` (text color) and `BackgroundColorSpan` (highlighting).
- **Compose Interoperability**: Wrapped in an `AndroidView`, allowing it to fit perfectly into a Compose UI while utilizing the `SpannableStringBuilder` engine.
- **Smart Selection Tracking**: Detects cursor movements and updates the UI state to reflect the formatting at the current cursor position.
- **Material 3 Integration**: Automatically syncs with your app's Material 3 color scheme (OnBackground, SurfaceVariant, etc.).

## 🛠 Technical Deep Dive

### 1. The `RichEditText` Bridge
The core is a private `RichEditText` class that extends the native `EditText`. It acts as a state-holder for "typing modes"—styles that are active but not yet applied to text.

### 2. The `restoreAndApply` Algorithm
Inside `doOnTextChanged`, I implemented a custom algorithm to manage span lifecycle:
- **Clean**: It clears overlapping spans in the current typing range to prevent "span bloating."
- **Restore**: It preserves spans from the previous state of the text.
- **Apply**: It injects new spans only into the newly added characters based on the active `typing` flags.


### 3. Programmatic Syncing
To prevent infinite loops between Compose state updates and the Native View, the editor uses an `isUpdatingProgrammatically` flag. This ensures that when the `content` state changes (e.g., loading from a database), the cursor position and span integrity remain intact.

## 📦 Tech Stack

- **Framework**: Jetpack Compose (Material 3)
- **Language**: Kotlin
- **DI**: Dagger Hilt
- **Processing**: KSP (Kotlin Symbol Processing)
- **Core APIs**: `AndroidView`, `SpannableStringBuilder`, `CharacterStyle`

## 📂 Project Structure

- `ui/editor/RichTextEditor.kt`: The primary component containing the `AndroidView` and span management logic.
- `ui/theme/`: Custom Material 3 theme implementation.
- `data/`: (Ready for Room/DataStore integration for persistence).

## 🚀 Getting Started

1. **Clone the Repo**:clone the repo
2. **Open in Android Studio**: Ladybug (2024.2.1) or newer recommended.
3. **Build**: The project uses KSP; ensure your libs.versions.toml is synced.
   
