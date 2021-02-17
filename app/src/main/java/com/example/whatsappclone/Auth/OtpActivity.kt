package com.example.whatsappclone.Auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.example.whatsappclone.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER="phoneNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber:String? =null
    private var timeLeft: Long = -1
    var mVerificationId:String?=null
    var mResendToken: PhoneAuthProvider.ForceResendingToken?=null
    private lateinit var progressDialog:ProgressDialog
    private var mCounterDown:CountDownTimer?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        initViews()
        startVerify()
    }

    private fun startVerify() {
        startPhoneNumberVerification(phoneNumber!!)
        showTimer(60000)
        progressDialog =createProgressDialog("Sending a verification code",false)
        progressDialog.show()
    }

    private fun showTimer(milliSecInFuture: Long) {
     resendBtn.isEnabled=false
        mCounterDown=object :CountDownTimer(milliSecInFuture,1000){
            override fun onFinish() {
            counterTv.isVisible=false
            resendBtn.isEnabled=true
            }

            override fun onTick(millisUntilFinished: Long){
                counterTv.isVisible=true
               counterTv.text=getString(R.string.Seconds_Remainning,millisUntilFinished/1000)
                  }

        }.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        if(mCounterDown!=null){
            mCounterDown!!.cancel()
        }
    }


    private fun initViews() {
        phoneNumber =intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text=getString(R.string.verify_number,phoneNumber)
        setSpannableString()

        //init click listener
        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }

                val smsCode:String? =credential.smsCode
                if(!smsCode.isNullOrBlank()){
                    sentcodeEt.setText(smsCode)
                }
              signInWithPhoneAuthCredential(credential)
            }


            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
                // Show a message and update the UI
                notifyUserAndRetry("Your Phone Number be wrong or connection error.Retry Again!")


            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                //for low level version which doesn't do auto verification save the verification code and the token

                progressDialog.dismiss()
                counterTv.isVisible = false
                // Save verification ID and resending token so we can use them later
                Log.e("onCodeSent==", "onCodeSent:$verificationId")

                mVerificationId = verificationId
                mResendToken= token

            }
        }
        }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        val mAuth=FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful){
                   showSignUpActivity()

                }
                else{
                    notifyUserAndRetry("Your Phone Number verification failed.Try again!!")
                }
            }

    }

    private fun notifyUserAndRetry(message: String) {
     MaterialAlertDialogBuilder(this).apply {
         setMessage(message)
         setPositiveButton("ok"){_,_->
             showLoginActivity()
         }
         setNegativeButton("Cancel"){dialog,_->
             dialog.dismiss()
         }
         setCancelable(false)
         create()
         show()
     }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("timeLeft", timeLeft)
        outState.putString(PHONE_NUMBER, phoneNumber)
    }

    // This method will send a code to a given phone number as an SMS
    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,            // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
    }

    private fun showSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun setSpannableString() {
        val span =SpannableString(getString(R.string.waiting_text,phoneNumber))
        val clickableSpan=object:ClickableSpan(){
            override fun onClick(widget: View) {
               //send back
                showLoginActivity()
                  }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText=false
                ds.color=ds.linkColor
            }
        }
        span.setSpan(clickableSpan,span.length - 13,span.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod=LinkMovementMethod.getInstance()
        waitingTv.text=span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)) //so there will be no back stack

    }

    override fun onBackPressed() {

    }

    override fun onClick(v: View?) {
        when(v){
            verificationBtn->{
                val code=sentcodeEt.text.toString()
                if(code.isNotEmpty() && !mVerificationId.isNullOrBlank()){
                    progressDialog=createProgressDialog("Please wait.....",false)
                    progressDialog.show()

                    val credential=PhoneAuthProvider.getCredential(mVerificationId!!,code)
                     signInWithPhoneAuthCredential(credential)

                }
                  }
            resendBtn->{
                val code=sentcodeEt.text.toString()
                if(mResendToken != null){
                    resendVerificationCode(phoneNumber.toString(),mResendToken)
                    showTimer(60000)
                    progressDialog=createProgressDialog("Sending a verification code wait.....",false)
                    progressDialog.show()
                }
            }
        }
       }
    private fun resendVerificationCode(
        phoneNumber: String,
        mResendToken: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            mResendToken
        ) // ForceResendingToken from callbacks
    }

}

fun Context.createProgressDialog(message:String,isCancelable:Boolean):ProgressDialog{
    return ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }
}