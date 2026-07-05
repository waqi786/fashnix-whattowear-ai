# Setup Guide

## Prerequisites

- Android Studio with JDK 17
- Android SDK 34
- Gradle 8.10.2 or the included Gradle wrapper
- Firebase project with Authentication, Firestore, Storage, Cloud Messaging, and Cloud Functions enabled
- Node.js 18 for Firebase Functions

## Android App

1. Clone the repository.
2. Open the project in Android Studio.
3. Create a Firebase Android app using package name `com.fashnix.app`.
4. Download `google-services.json` from Firebase Console.
5. Place it at `app/google-services.json`.
6. Add private local values in `gradle.properties` or your local Gradle user properties:

```properties
weather.api.key=YOUR_WEATHER_API_KEY
cloud.function.url=YOUR_FIREBASE_CALLABLE_OR_HTTPS_FUNCTION_URL
```

7. Build the debug APK:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Firebase Functions

Install dependencies:

```bash
cd functions
npm install
```

Set the OpenAI key as a Firebase secret:

```bash
firebase functions:secrets:set OPENAI_API_KEY
```

Deploy functions and rules:

```bash
firebase deploy
```

## Security

Do not commit release keys, local Gradle properties, real `google-services.json`, `.env` files, or generated build folders.
