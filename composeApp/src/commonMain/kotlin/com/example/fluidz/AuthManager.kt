package com.example.fluidz

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AuthManager {
    fun signInWithGoogle(onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun signInWithApple(onSuccess: (String) -> Unit, onError: (String) -> Unit)
}
