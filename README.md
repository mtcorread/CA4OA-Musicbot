# 🎵 CA4OA - MusicBot

## Introduction  
MusicBot is an AI-powered app that combines intelligent chat features with music playback functionality. 
Designed to test the acceptance of AI technology among older adults, MusicBot also aims to evoke emotions and stimulate cognitive engagement through music and conversation. 🎶

## Features  
- 💬 **AI Chat**: Enjoy interactive conversations with the AI about music.
- 🎧 **Music Playback**: Play music directly within the app.
- 🔄 **Voice and Text Support**: Switch between voice commands or typing.
- 🌈 **Customizable UI**: Choose from dark/light themes and adjust text size.

## Development Environment and Tools  

### 🖥️ Operating System: Android  
MusicBot is developed for Android, allowing wide compatibility and access to multimedia features.

### 💻 Programming Language: Java  
Built in Java for robust, modular code and platform independence.

## Architecture: 🏗️ Model-View-ViewModel (MVVM)  
MusicBot is designed using the MVVM architecture, making the app modular, scalable, and easy to maintain.

- **Model**: Handles data related to music tracks, user queries, and APIs.
- **ViewModel**: Manages state and data flow between the UI and data layer.
- **View**: The UI layer, responsible for interacting with the user.

## APIs  
MusicBot integrates two primary APIs:
- **OpenAI API**: Powers the chatbot’s natural language processing for conversational AI.
- **Deezer API**: Handles music search, metadata retrieval, and streaming within the app.

## Key Functionality

### 💬 Chat Interaction  
- **Text and Voice Input**: Users can communicate with the AI via text or voice, which is processed and responded to in real-time.
- **Natural Language Processing**: The OpenAI API allows MusicBot to understand and respond to user queries, offering relevant music suggestions and general conversation.

### 🎧 Music Playback  
- **Deezer API Integration**: MusicBot uses the Deezer API to search and play songs. 
- **Music Search**: Find songs based on user queries. Play or suggest music using song previews directly in the app.

## UI and Design  
- 🎨 **Morandi Color Scheme**: The UI features soft, muted tones of purple and grey, designed for readability and a pleasing aesthetic.
- 🖱️ **User-Friendly Controls**: Large buttons, easy-to-read text, and voice input functionality ensure accessibility for all users.

## How to Use

1. **🔑 Set Up the API Keys**  
   - Open `ChatViewModel.java` file.
   - Replace the placeholder with your OpenAI API key.
   - Open `DeezerHelper.java` file.
   - Replace the placeholder with your OpenAI API key in the "musicSearch" method.

2. **📲 Install the App**
   - Clone the repository and open the project in Android Studio.
   - Build and run the app on an Android device or emulator.
