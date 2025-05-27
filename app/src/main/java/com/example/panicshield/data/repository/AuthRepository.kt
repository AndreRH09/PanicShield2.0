package com.example.panicshield.data.repository

import com.example.panicshield.domain.model.AuthResult
import com.example.panicshield.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    fun getAuthState(): Flow<Boolean>
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(email: String, password: String, displayName: String): AuthResult
    suspend fun signOut()
    fun getCurrentUser(): User?
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun getAuthState(): Flow<Boolean> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = getUserFromFirestore(firebaseUser.uid) ?: firebaseUser.toUser()
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Error al iniciar sesión")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    createdAt = System.currentTimeMillis()
                )

                // Guardar usuario en Firestore
                saveUserToFirestore(user)

                AuthResult.Success(user)
            } else {
                AuthResult.Error("Error al crear la cuenta")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUser(): User? {
        return auth.currentUser?.toUser()
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection("users")
                .document(user.uid)
                .set(user)
                .await()
        } catch (e: Exception) {
            // Log error but don't throw, user is already created in Auth
        }
    }

    private suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            uid = uid,
            email = email ?: "",
            displayName = displayName ?: "",
            phoneNumber = phoneNumber ?: ""
        )
    }
}