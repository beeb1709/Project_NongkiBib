# NongkiBib - Community Hub for BIB Awardees

NongkiBib is an Android application designed to connect Beasiswa Indonesia Bangkit (BIB) awardees. It features scholarship management, event tracking, real-time chat, and location-based services.

## Project Structure

- `/app`: Android Frontend (Kotlin, Material 3, Jetpack Compose components).
- `/backend-nongkibib`: Backend API (Node.js, Express, TypeScript, MariaDB).

## Getting Started

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend-nongkibib
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Configure environment variables:
   - Copy `.env.example` to `.env`.
   - Fill in your database credentials and Google OAuth keys.
4. Run the server:
   ```bash
   npm run dev
   ```

### Android App Setup

1. Open the project in Android Studio.
2. Update the `BASE_URL` in `app/src/main/java/com/example/nongkibib/api/ApiClient.kt` to match your local IP or public tunnel (ngrok).
3. Build and run on your Android device (minSdk 35).

## Features

- **Scholarship & Event Discovery**: Explore and register for various academic and community opportunities.
- **Real-time Chat**: Connect with fellow awardees via WebSocket-powered messaging.
- **Map Interaction**: Discover hangout spots and see nearby friends using Google Maps integration.
- **Secure Auth**: Integration with Google Sign-In and SHA-256 hashed local passwords.
- **Gamification**: Earn BIB Points by participating in events and verifying student status.

## Technologies Used

- **Frontend**: Kotlin, Retrofit 2, Material 3, Google Play Services.
- **Backend**: Node.js, Express, TypeScript, MySQL/MariaDB, WebSocket (ws).
- **Security**: SHA-256 Hashing, Google OAuth 2.0, EncryptedSharedPreferences.
