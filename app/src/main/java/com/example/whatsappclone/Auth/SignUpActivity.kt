package com.example.whatsappclone.Auth

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.whatsappclone.MainActivity
import com.example.whatsappclone.R
import com.example.whatsappclone.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var downloadUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        userImgView.setOnClickListener {
         checkPermissionForImage()
            //firebase extension - Image Thumbnail
        }

        nextBtn.setOnClickListener {
            //disable next button
            nextBtn.isEnabled=false
            //first check whether name or image is empty or not
            val name=nameEt.text.toString()
            if(name.isEmpty()){
                Toast.makeText(this,"Name cannot be empty",Toast.LENGTH_SHORT).show()
            }
            else if(!::downloadUrl.isInitialized){
                Toast.makeText(this,"Image cannot be empty",Toast.LENGTH_SHORT).show()
            }
            else{
                //upload user in database
                val user=
                    User(name, downloadUrl, downloadUrl, auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                  startActivity(
                      Intent(this, MainActivity::class.java)
                  )
                    finish()
                }.addOnFailureListener {
                 nextBtn.isEnabled=true
                }
            }

        }
    }

    private fun checkPermissionForImage() {

        if((checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            && (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        ){
            val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(
                permission,
                1001
            )//Give an integer value for permission code read like 1001
            requestPermissions(
                permissionWrite,
                1002
            )//give an integer value for permission code write like 1002
        }
        else{
            pickImageFromGallery()
        }

    }

    private fun pickImageFromGallery() {
    val  intent= Intent(Intent.ACTION_PICK);
    intent.type="image/*"
    startActivityForResult(
        intent,1000
    )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            data?.data?.let {
                userImgView.setImageURI(it)
                startUpload(it) //for uploading image in firebase storage
            }
        }
    }
    private fun startUpload(filePath: Uri) {
        nextBtn.isEnabled = false
        val ref = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = ref.putFile(filePath)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                nextBtn.isEnabled = true
            } else {
                nextBtn.isEnabled = true
                // Handle failures
            }
        }.addOnFailureListener {

        }
    }
}
