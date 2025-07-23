# Smart Surveillance System - Mobile App

A mobile application for accident detection and emergency response, built with Kotlin for Android.

## Features

### Authentication System
- User login with email and password
- New user registration with email verification
- Password reset functionality
- Form validation and error handling
- Terms & Conditions acceptance

### Accident Detection & Monitoring
- Real-time accident detection using device sensors
  - Accelerometer for impact detection
  - Gyroscope for device orientation
  - Configurable sensitivity thresholds
- Monitoring status toggle with state persistence
- Countdown timer for accident confirmation/cancellation
- Background monitoring capability

### Emergency Response
- Automatic emergency contact notification
  - SMS alerts with location
  - Emergency calls
- Contact picker integration with device contacts
- Multiple emergency contacts support
- Customizable alert settings per contact
  - SMS toggle
  - Call toggle
- Location sharing functionality

### Alert History
- Chronological list of all detected incidents
- Detailed incident information:
  - Timestamp
  - Location coordinates
  - Alert type
  - Alert status (Sent/Cancelled)
  - Notified contacts

### Settings & Configuration
- Dark/Light theme toggle with system default option
- Accident detection sensitivity adjustment
  - Acceleration threshold
  - Rotation threshold
- Emergency contact preferences
  - Automatic SMS alerts toggle
  - Automatic emergency calls toggle
- Settings state persistence

## Screens

### Login Screen
- Email field with validation
- Password field with validation
- "Forgot Password?" option
- "Login" button
- "Sign Up" link to registration

### Registration Screen
- Email field with validation
- Password & Confirm Password fields with validation
- Terms & Conditions checkbox
- "Register" button
- "Already have an account? Login" link

### Main Screen
- Monitoring status toggle
- Current monitoring state indicator
- Emergency contacts management
- Location sharing button
- Alert history display
- Settings access

### Emergency Contacts Screen
- Add contact button with device contact picker
- Contact list with:
  - Contact name
  - Phone number
  - Relationship
  - SMS/Call preference toggles
- Delete contact option

### Settings Screen
- Theme selection
- Accident detection sensitivity sliders
- Emergency notification preferences
- App configuration options

## Technical Details

### Technology Stack
- **Language**: Kotlin
- **Architecture**: MVVM with Firebase integration
- **UI**: Material Design 3 components
- **Authentication**: Firebase Authentication
- **Database**: Cloud Firestore
- **Minimum SDK**: 21 (Android 5.0 Lollipop)

### Key Components
- Firebase Authentication for user management
- Cloud Firestore for data persistence
- Device sensors integration
- Runtime permissions handling
- SharedPreferences for local settings
- Background service capability
- Material Design theming

### Required Permissions
- Location access (Fine location)
- SMS sending
- Phone calls
- Contact access
- Background processing

## Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 8 or newer
- Google Play Services
- Firebase project configuration

### Building the Project
1. Clone the repository
2. Set up Firebase:
   - Create a new Firebase project
   - Add your Android app to Firebase
   - Download `google-services.json`
   - Place it in the app directory
3. Open the project in Android Studio
4. Build and run on an emulator or physical device

### Configuration
- Adjust sensitivity thresholds in settings
- Set up emergency contacts
- Configure notification preferences
- Enable required permissions when prompted

## Security Features
- Email verification
- Secure password storage
- Protected user data
- Permission-based access control
- Encrypted data transmission

## Next Steps for Development

- Implement actual authentication with Firebase or a custom backend
- Add surveillance features
- Create dashboard for camera feeds
- Implement notifications
- Add user profile management 