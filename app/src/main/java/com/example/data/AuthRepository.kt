package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.model.UserAccount
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

  private val tag = "AuthRepository"

  /**
   * Admin allow-list, read from string resources rather than hardcoded.
   *
   * The original check compared against literal email addresses baked into the
   * source. That is wrong for a public repository (it publishes the owner's
   * identity and any contributor's fork silently inherits their privileges) and
   * wrong for security: `email.contains("admin")` let anyone self-register as an
   * administrator.
   *
   * Prefer a Firestore `users/{uid}.isAdmin` document or a Firebase custom
   * claim, both of which are server-controlled. This resource is the fallback
   * for local development only — see res/values/admin_emails.xml and
   * docs/CONFIGURATION.md.
   */
  private val adminEmails: Set<String> by lazy {
    val array = context.resources.getStringArray(R.array.admin_emails)
    array.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
  }

  private fun isAdminEmail(email: String): Boolean =
    email.isNotBlank() && email.lowercase() in adminEmails
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  private val credentialManager: CredentialManager = CredentialManager.create(context)

  // Default `(default)` database of the project in google-services.json.
  private val firestore: FirebaseFirestore by lazy {
    FirebaseFirestore.getInstance()
  }

  // Observe current user state
  fun observeCurrentUser(): Flow<UserAccount?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { fbAuth ->
      val user = fbAuth.currentUser
      if (user == null) {
        trySend(null)
      } else {
        val email = user.email ?: ""
        // Check if designated admin email or saved in Firestore
        val isAdminEmail = isAdminEmail(email)

        val account = UserAccount(
          uid = user.uid,
          name = user.displayName ?: "مستخدم AppStore",
          email = email,
          photoUrl = user.photoUrl?.toString() ?: "",
          isAdmin = isAdminEmail
        )
        trySend(account)
      }
    }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
  }

  fun getCurrentUser(): UserAccount? {
    val user = auth.currentUser ?: return null
    val email = user.email ?: ""
    val isAdmin = isAdminEmail(email)

    return UserAccount(
      uid = user.uid,
      name = user.displayName ?: "مستخدم AppStore",
      email = email,
      photoUrl = user.photoUrl?.toString() ?: "",
      isAdmin = isAdmin
    )
  }

  suspend fun signInWithGoogle(): Result<UserAccount> {
    return try {
      val webClientId = context.getString(R.string.default_web_client_id)

      val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(context = context, request = request)
      val credential = result.credential

      if (credential is GoogleIdTokenCredential) {
        val googleIdToken = credential.idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = auth.signInWithCredential(authCredential).await()
        val user = authResult.user

        val email = user?.email ?: ""
        val isAdmin = isAdminEmail(email)

        val account = UserAccount(
          uid = user?.uid ?: "",
          name = user?.displayName ?: "مستخدم AppStore",
          email = email,
          photoUrl = user?.photoUrl?.toString() ?: "",
          isAdmin = isAdmin
        )

        // Save profile in Firestore
        if (user != null) {
          firestore.collection("users").document(user.uid).set(account)
        }

        Result.success(account)
      } else {
        Result.failure(Exception("Unsupported credential type"))
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w(tag, "Google sign-in was cancelled by the user")
      Result.failure(e)
    } catch (e: Exception) {
      Log.e(tag, "Google sign-in failed", e)
      Result.failure(e)
    }
  }

  fun signOut() {
    auth.signOut()
  }
}
