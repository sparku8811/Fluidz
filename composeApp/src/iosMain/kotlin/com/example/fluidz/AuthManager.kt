package com.example.fluidz

actual class AuthManager {
    actual fun signInWithGoogle(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        // iOS implementation using GoogleSignIn SDK for iOS
        onError("Google Sign-In on iOS requires native SDK integration.")
    }

    actual fun signInWithApple(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        // iOS implementation using AuthenticationServices framework
        onError("Apple Sign-In on iOS requires native framework integration.")
    }
}
