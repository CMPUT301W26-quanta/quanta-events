# Quanta Events

Quanta Events is an Android app for discovering, creating, and managing events with role-based experiences for entrants, organizers, and admins. It pairs a native Android client with Firebase Cloud Functions for server-side workflows and notifications.

## Features

- Browse events and view details
- Join or leave event waitlists
- Organizer tools to create, edit, and manage events
- Notifications and messaging to keep users informed
- Admin views for managing users, events, and images

## Tech Stack

- Android (Java)
- Firebase Firestore, Cloud Functions, and Messaging
- TypeScript (Firebase Functions)
- Zod for validation

## Project Structure

- `src/` Android application (Gradle project)
- `server/functions/` Firebase Cloud Functions (TypeScript)
- `docs/` CRC Cards and UML Diagram

## Getting Started

### Android App

1. Open the Android project in Android Studio using the `src/` directory.
2. Sync Gradle and ensure you are using JDK 11.
3. Configure Firebase for your project and place your `google-services.json` in `src/app/`.
4. Run the app on an emulator or device.

### Firebase Functions

1. Install Node.js (version 24) and the Firebase CLI.
2. From `server/functions/`:

```bash
npm install
npm run build
```

## To deploy firebase functions

1. Run the command
```bash
firebase deploy
```

## Documentation

CRC cards and UML Diagrams can be found in `docs/`.

