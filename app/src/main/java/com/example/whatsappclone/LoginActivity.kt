package com.example.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*
import androidx.core.widget.addTextChangedListener as addTextChangedListener1

class LoginActivity : AppCompatActivity() {

    private lateinit var phoneNumber:String
    private lateinit var countryCode:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneNumberEt.addTextChangedListener1 {
            if (it != null) {
                nextBtn.isEnabled = (it.length >=10 && !it.isNullOrEmpty())
            }
        }
        nextBtn.setOnClickListener {
            checkNumber()
        }
        }

    private fun checkNumber() {
    countryCode = ccp.selectedCountryCodeWithPlus
    phoneNumber =countryCode + phoneNumberEt.text.toString()

     notifyUser()    //Make the dialog box and show it to user
    }

    private fun notifyUser() {
       MaterialAlertDialogBuilder(this).apply {
           setMessage("We will be verifying the phone number:$phoneNumber\n" +
                   "Is this OK, or would you like to edit the number?")
           setPositiveButton("Ok"){_,_ ->
               showOptActivity()
           }
           setNegativeButton("Edit"){dialog,which ->
               dialog.dismiss()
           }
           setCancelable(false)
           create()
           show()
       }
         }

    private fun showOptActivity() {
        startActivity(Intent(this,OtpActivity::class.java).putExtra(PHONE_NUMBER,phoneNumber))
        finish() //so activity remove from back stack
        }
}