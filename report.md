# Smart Surveillance System - Mobile App Report

## Overview
This report provides an explanation of each file and folder in the Smart Surveillance System mobile app project. The app is built using Kotlin for Android and integrates Firebase for authentication, Firestore for data storage, and various Android components for real-time accident detection and emergency response.

## Project Structure

### Root Directory
- **README.md**: Contains project overview, features, screens, technical details, and setup instructions.
- **build.gradle**: Main build configuration file for the project, defining dependencies and build settings.
- **settings.gradle**: Specifies project settings and included modules.
- **gradle.properties**: Contains Gradle-specific properties.
- **local.properties**: Stores local SDK path and other local configurations.
- **gradlew** and **gradlew.bat**: Gradle wrapper scripts for building the project on Unix and Windows systems, respectively.
- **google-services.json**: Configuration file for Firebase integration.
- **firebase.json**: Configuration for Firebase services.
- **.firebaserc**: Firebase project configuration.
- **.gitignore**: Specifies files and directories to be ignored by Git.

### Directories
- **.idea/**: Contains IntelliJ IDEA/Android Studio project settings.
- **.gradle/**: Contains Gradle build cache and temporary files.
- **gradle/**: Contains Gradle wrapper files.
- **functions/**: Likely contains Firebase Cloud Functions code.
- **build/**: Contains build outputs and intermediate files.
- **app/**: Main application module containing source code, resources, and configuration.

### App Module (`app/`)
- **build.gradle**: App-specific build configuration, including dependencies and build settings.
- **google-services.json**: Firebase configuration for the app module.
- **src/**: Contains the main source code and resources.

### Source Code (`app/src/main/`)
- **java/**: Contains Kotlin/Java source files.
  - **com/app/smart/**: Main package containing app logic.
    - **MainActivity.kt**: Main screen/activity for accident detection, monitoring, and emergency response.
      - **Working Principle**: Implements `SensorEventListener` to monitor device sensors (accelerometer, gyroscope) for accident detection. Uses Firebase for authentication and Firestore for data storage. Manages permissions, background monitoring, and user preferences. Handles emergency logic: SMS/call, location, countdown, cooldown.
    - **SettingsActivity.kt**: Manages app settings (theme, sensitivity, notification preferences).
      - **Working Principle**: Allows users to customize app behavior and appearance, including theme selection and sensitivity adjustments.
    - **EmergencyContactsActivity.kt**: Manages emergency contacts (add, edit, delete, preferences).
      - **Working Principle**: Lets users configure who is notified in emergencies, including contact picker integration and customizable alert settings.
    - **LoginActivity.kt**: Handles user login.
      - **Working Principle**: Manages user authentication with email and password, including form validation and error handling.
    - **RegisterActivity.kt**: Handles new user registration.
      - **Working Principle**: Manages new user registration with email verification, including form validation and terms acceptance.
    - **TermsActivity.kt**: Displays terms and conditions.
      - **Working Principle**: Shows the terms and conditions for user acceptance.
    - **SmartApp.kt**: Application class for app-wide initialization.
      - **Working Principle**: Initializes app-wide settings and configurations.
    - **viewmodels/**: Contains ViewModel classes.
      - **MainViewModel.kt**: MVVM ViewModel for main activity logic/state.
        - **Working Principle**: Decouples UI from business logic, managing state and data for the main activity.
    - **dialogs/**: Contains custom dialog classes.
      - **PasswordConfirmDialog.kt**: Dialog for password confirmation.
        - **Working Principle**: Used for sensitive actions, such as deleting alerts, requiring password confirmation.
    - **data/**: Contains data model classes.
      - **EmergencyContact.kt**: Model for emergency contacts.
        - **Working Principle**: Represents an emergency contact with properties like name, phone number, and relationship.
      - **AlertEvent.kt**: Model for alert events.
        - **Working Principle**: Represents an alert event with properties like timestamp, location, and status.
    - **adapters/**: Contains RecyclerView adapters.
      - **EmergencyContactsAdapter.kt**: Adapter for displaying emergency contacts.
        - **Working Principle**: Binds emergency contact data to the RecyclerView for display.
      - **AlertHistoryAdapter.kt**: Adapter for displaying alert history.
        - **Working Principle**: Binds alert event data to the RecyclerView for display.
    - **services/**: Likely contains background service classes (currently empty).
- **res/**: Contains app resources.
  - **layout/**: Contains XML layout files for activities, dialogs, and list items.
  - **values/**: Contains XML files for strings, colors, and themes.
  - **drawable/**: Contains image resources.
  - **mipmap/**: Contains app icons for different densities.
  - **menu/**: Contains menu XML files.
  - **animator/**: Contains animation XML files.
  - **font/**: Contains font resources.
- **AndroidManifest.xml**: Declares app components, permissions, and configuration.

## Conclusion
This report provides a comprehensive overview of the Smart Surveillance System mobile app project, explaining the purpose and role of each file and folder, along with their working principles. For further details, refer to the individual files and their respective documentation.