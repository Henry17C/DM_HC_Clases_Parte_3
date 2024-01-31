package com.examenp.biometricapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.examenp.biometricapp.viewmodels.MainViewModel
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {


    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var btnFinger: ImageView
    private lateinit var  txtInfo: TextView

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListeners()
        initObservables()
        AutificationVaribles()
        mainViewModel.checkBiometric(this)

    }




    private fun initListeners(){

         btnFinger= findViewById<ImageView>(R.id.imgFinger)
        txtInfo= findViewById(R.id.txtInfo)

        btnFinger.setOnClickListener{
            biometricPrompt.authenticate(promptInfo)

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
                   btnFinger.visibility= View.VISIBLE
                   txtInfo.text=getString(R.string.biometric_succes)

               }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE->{

                    txtInfo.text = getString(R.string.biometric_no_hardware)
       
                }


                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE->{
                    Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                }


                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED->{
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                    }
                    startActivityForResult(enrollIntent, 100)
                }

               }
            }
        }

    }




