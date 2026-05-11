# Rich Text Editor App

A feature-rich text editor Android application built with **Kotlin** and **Jetpack Compose**, designed for creating and editing beautifully formatted documents with an intuitive user interface.

## 📋 Overview

TextEditorApp is a modern Android text editor that allows users to create, edit, and manage multiple documents with advanced text formatting capabilities. The app leverages Jetpack Compose for a responsive UI and Room Database for persistent storage, ensuring all your documents are saved locally on your device.

## ✨ Features

### Document Management
- **Create & Edit Documents** - Create new documents and edit existing ones seamlessly
- **Document List View** - Browse all your documents in a clean, organized list with previews
- **Auto-save** - Documents are automatically saved as you type
- **Delete Documents** - Remove unwanted documents with a confirmation dialog
- **Document Preview** - See document titles and previews in the list view
- **Timestamps** - Track when documents were created and last updated

### Rich Text Formatting
- **Bold Text** - Apply bold styling to selected text
- **Italic Text** - Add italic emphasis to your content
- **Underline** - Underline important sections
- **Strikethrough** - Mark text as deleted or outdated with strikethrough
- **Text Color** - Choose from 12+ text colors to customize your content
- **Highlight Color** - Add colored backgrounds to text with 12 highlight colors
- **Typing Continuity** - Formatting is maintained while you continue typing

### User Experience
- **Seamless Navigation** - Smooth transitions between document list and editor
- **Material Design 3** - Modern, polished UI with Material Design principles
- **Edge-to-Edge Display** - Utilizes full screen real estate with edge-to-edge layouts
- **Empty State** - Helpful guidance when no documents exist
- **Real-time Formatting** - See text formatting updates instantly
- **Selection-based Formatting** - Select text and apply formatting on the fly

## 🏗️ Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation**: Jetpack Navigation Compose
- **Database**: Room Database
- **Dependency Injection**: Hilt
- **State Management**: Flow & StateFlow
- **Design System**: Material Design 3

### Project Structure
```com.example.texteditor/ 
├── ui/ 
│ ├── editor/ 
│ │ ├── EditorScreen.kt # Main editor screen 
│ │ ├── EditorViewModel.kt # Editor state & business logic
│ │ ├── EditorUiState.kt # Editor UI state model 
│ │ ├── RichTextEditor.kt # Rich text input field component 
│ │ └── ColorPickerBottomSheet.kt # Color selection UI 
│ ├── documentlist/ 
│ │ ├── DocumentListScreen.kt # Document list screen 
│ │ └── DocumentListViewModel.kt # List state & logic 
│ ├── navigation/ 
│ │ └── AppNavGraph.kt # App navigation structure 
│ └── theme/ 
│ └── RichTextEditorTheme.kt # Material Design 3 theme 
├── data/ │ ├── db/ 
│ │ ├── AppDatabase.kt # Room database configuration 
│ │ ├── DocumentEntity.kt # Document data model 
│ │ └── DocumentDao.kt # Database access object 
│ ├── DocumentRepository.kt # Data repository layer 
│ └── SpanSerializer.kt # Text span serialization (formatting) 
├── di/ │ └── AppModule.kt # Dependency injection setup 
├── MainActivity.kt # App entry point 
└── App.kt # Application class
```


### Key Components

#### EditorViewModel
Manages the editor's state including:
- Document title and content
- Active formatting states (bold, italic, underline, strikethrough)
- Active colors (text color, highlight color)
- Text selection state
- Save status

#### SpanSerializer
Handles text formatting serialization:
- Converts text with formatting to HTML for storage
- Restores formatted text from HTML
- Detects active formatting in selected text ranges
- Manages style spans (bold, italic, underline, strikethrough)
- Manages color spans (text color, highlight)

#### DocumentRepository
Provides CRUD operations for documents:
- Retrieve all documents
- Fetch document by ID
- Save new documents
- Update existing documents
- Delete documents

## 🚀 Getting Started

### Prerequisites
- Android Studio 2023.1 or later
- Android SDK 24 (Android 7.0) or higher
- Kotlin 1.9+

### Installation

1. Clone the repository:
   git clone https://github.com/namanmawandia/TextEditorApp.git
2. Open the project in Android Studio
3. Build and run the application:
   Click Run → Run 'app' or press Shift + F10
