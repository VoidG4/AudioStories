# Audio Stories
# 🎧 AudioStories

**AudioStories** is an interactive, multimodal Android application built entirely with **Jetpack Compose** and **Kotlin**. It is designed to bring children's stories to life by combining rich UI layouts, real-time Text-to-Speech (TTS) narration, synchronized text highlighting, and hands-free voice navigation.

![AudioStories Banner](https://img.shields.io/badge/Android-Jetpack_Compose-4CAF50?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue?style=for-the-badge&logo=kotlin)
![Firebase](https://img.shields.io/badge/Firebase-Firestore-FFCA28?style=for-the-badge&logo=firebase)

---

## ✨ Key Features

* **Interactive Story Player:** Uses the native Android TTS engine to narrate stories. Features custom playback controls including Play/Pause, Seek, Restart, and adjustable Playback Speed (0.75x, 1x, 1.5x, 2.0x).
* **Real-time Text Highlighting:** As the audio plays, the text automatically scrolls and highlights the currently spoken section, providing an immersive reading experience.
* **Voice-Controlled Navigation:** Integrated `SpeechRecognizer` allows users to navigate the app completely hands-free. Includes beautiful, state-driven microphone animations (pulse and breathe effects).
* **Cloud-Synced Content:** Stories are fetched in real-time from a remote **Firebase Firestore** database.
* **Anonymous User Tracking:** Generates a unique Device UUID to seamlessly track user history, listening progress, and favorite stories without requiring a complex authentication flow.
* **Personalized Statistics Dashboard:** A beautifully animated dashboard displaying listening habits, total estimated listening time, recent history, and a dedicated favorites list.
* **Dynamic Localization:** In-app language switcher supporting **English**, **Greek**, and **German**, updating the UI locale instantly.

---

## 📸 Screenshots

| Home Screen | Story Player & Highlighting | Voice Assistant | Statistics Dashboard |
| :---: | :---: | :---: | :---: |
| *[Insert Screenshot Here]* | *[Insert Screenshot Here]* | *[Insert Screenshot Here]* | *[Insert Screenshot Here]* |

---

## 🛠️ Tech Stack & Architecture

The application strictly adheres to modern Android Development guidelines, utilizing the **MVVM (Model-View-ViewModel)** architecture pattern.

* **UI:** Jetpack Compose, Material Design 3, Coil (Async Image Loading).
* **Architecture & State:** MVVM, Kotlin Coroutines, StateFlow.
* **Backend:** Firebase Cloud Firestore.
* **Hardware APIs:** Android `TextToSpeech` (TTS) API, Android `SpeechRecognizer` API.
* **Navigation:** Jetpack Navigation Compose.

---

## 🚀 Getting Started

To run this project locally, you will need Android Studio (Giraffe or newer recommended).

### 1. Clone the repository
```bash
git clone [https://github.com/VoidG4/AudioStories.git](https://github.com/VoidG4/AudioStories.git)
```

### 2. Firebase Setup
Since this project uses Firebase Firestore for remote data fetching, you need to connect it to your own Firebase project:
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new Android App project.
3. Download the `google-services.json` file.
4. Place the `google-services.json` file inside the `app/` directory of this project.
5. Create a Firestore Database with a `stories` collection.

### 3. Build and Run
* Open the project in Android Studio.
* Sync the Gradle files.
* Run the app on an Emulator or a physical Android device (API 26+). *Note: A physical device or a properly configured emulator with Google Play Services is required for the Voice Recognition and TTS to function optimally.*

---

## 🏗️ Project Structure

* `data/`: Contains Data classes (`Story.kt`) and the ViewModel handling Firestore operations.
* `ui/`: Contains all Jetpack Compose screens (`HomeScreen.kt`, `StoryPlayerScreen.kt`, etc.) and reusable UI components.
* `utils/`: Contains utility classes like the `VoiceAssistant.kt` state machine and `DeviceIdManager.kt`.
* `theme/`: Contains the styling, typography, and custom color palettes.

---
