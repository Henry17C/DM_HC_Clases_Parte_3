package com.examenp.biometricapp
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.examenp.biometricapp.databinding.ActivityMainBinding
import com.examenp.biometricapp.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

      lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth ///FIREBASE

    private val mainViewModel: MainViewModel by viewModels()

//    private lateinit var btnFinger: ImageView
//   private lateinit var  txtInfo: TextView

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        initListeners()
        initObservables()
        AutificationVaribles()
        mainViewModel.checkBiometric(this)

    }


    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
           // startActivity(Intent(this@MainActivity,MainActivity2::class.java))
            binding.textUser.visibility= View.GONE
            binding.textPassword.visibility= View.GONE

            binding.imgFinger.visibility=View.VISIBLE
            binding.txtInfo.text=getString(R.string.biometric_succes)


        }else{
            binding.imgFinger.visibility= View.GONE
            binding.txtInfo.text=getString(R.string.no_user)
        }
    }

    private fun initListeners(){


        binding.imgFinger
        binding.imgFinger.setOnClickListener{
            biometricPrompt.authenticate(promptInfo)

        }

        binding.btnSaveUser.setOnClickListener {
            createNewUsers(binding.textUser.text.toString(), binding.textPassword.text.toString())
        }
        binding.btnSignUser.setOnClickListener {

            SignInUsers(binding.textUser.text.toString(),binding.textPassword.text.toString())

        }

    }

    private fun AutificationVaribles(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(Intent(this@MainActivity,MainActivity2::class.java))
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }





    private fun initObservables(){
        mainViewModel.resultCheckBiometric.observe(this){code->
            when(code){
               BiometricManager.BIOMETRIC_SUCCESS->{

                   binding.imgFinger.visibility= View.VISIBLE
                   binding.txtInfo
                   binding.txtInfo.text=getString(R.string.biometric_succes)

               }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE->{

                    binding.txtInfo.text = getString(R.string.biometric_no_hardware)
       
                }


                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE->{
                    Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                }


                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED->{
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }
                    startActivityForResult(enrollIntent, 100)
                }

               }
            }
        }


    private fun createNewUsers(user:String, password:String){
        auth.createUserWithEmailAndPassword(user,password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "createUserWithEmail:success")
                    val user = auth.currentUser
                    Snackbar.make(this,binding.textUser,"CreateUserWithEmail: Success",Snackbar.LENGTH_LONG).show()
                    binding.textUser.text.clear()
                    binding.textPassword.text.clear()

                } else {
                    // If sign in fails, display a message to the user.
                    Snackbar.make(this,binding.textUser,task.exception!!.message.toString(),Snackbar.LENGTH_LONG).show()

                }
            }
    }



    private fun SignInUsers(email:String,password:String){

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    startActivity(Intent(this@MainActivity,MainActivity2::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Snackbar.make(this,binding.textUser,"signInWithEmail:failure",Snackbar.LENGTH_LONG).show()

                }
            }
    }

    }




