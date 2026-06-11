package com.example.fluidz

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AuthManager {
    actual fun signInWithGoogle(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        // Desktop implementation would typically use a local web server for OAuth callback
        onError("Google Sign-In on Desktop is currently under development.")
    }

    actual fun signInWithApple(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        onError("Apple Sign-In on Desktop is currently under development.")
    }
}
