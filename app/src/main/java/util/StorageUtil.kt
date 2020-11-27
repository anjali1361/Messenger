package util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

object StorageUtil {
    private val storageInstance :FirebaseStorage by lazy {FirebaseStorage.getInstance()}

    private val currentUserRef: StorageReference
        get() = storageInstance.reference.child(FirebaseAuth.getInstance().currentUser?.uid!! ?: throw NullPointerException("Uid is null"))//read only property

    fun uploadProfilePhoto(imageBytes:ByteArray,onSuccess:(imagePath:String) -> Unit){//upload to firebaseStorage
        val ref = currentUserRef.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes).addOnSuccessListener {
            onSuccess(ref.path)
        }
    }

    fun uploadMessagePhoto(imageBytes: ByteArray, onSuccess:(imagePath:String) -> Unit){
        val ref = currentUserRef.child("messages/${UUID.nameUUIDFromBytes(imageBytes)}")//all users will have a directory messages in which image messages r stored
        ref.putBytes(imageBytes).addOnSuccessListener {
            onSuccess(ref.path)
        }

    }

    fun pathToReference(path:String) = storageInstance.getReference(path)//path of image stored in storage
}