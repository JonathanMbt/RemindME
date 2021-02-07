package com.upriseus.remindme

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.upriseus.remindme.features.Hash
import java.util.concurrent.Executor

class SignActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        executor = ContextCompat.getMainExecutor(this)
        val type = intent.getStringExtra("type").toString()
        val username = findViewById<TextInputEditText>(R.id.username_text)
        val password = findViewById<TextInputEditText>(R.id.username_password)
        val stayconnect = findViewById<SwitchMaterial>(R.id.stay_connect)
        val title = findViewById<TextView>(R.id.main)
        val buttonConnect = findViewById<Button>(R.id.connect)
        val biometricButton = findViewById<Button>(R.id.connect_biometrics)

        val biometricManager = BiometricManager.from(this)
        when(biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

            }
        }

        if(type == "signin"){
            stayconnect.visibility = View.VISIBLE
            biometricButton.visibility = View.VISIBLE
            title.text = getString(R.string.sign_in)
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(applicationContext,
                            "Authentication error: $errString", Toast.LENGTH_SHORT)
                            .show()
                    }
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(applicationContext,
                            "Authentication succeeded!", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(applicationContext, HomeActivity::class.java)
                        startActivity(intent)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(applicationContext, "Authentication failed",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build()
        }else{
            title.text = getString(R.string.sign_up)
        }

        biometricButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        buttonConnect.setOnClickListener {
            if(type == "signin"){
                checkAuthentification(username.text.toString(), password.text.toString(), stayconnect.isChecked)
            }else{
                createAccount(username.text.toString(), password.text.toString())
            }
        }

    }

    private fun checkAuthentification(username: String, password: String, stay : Boolean){
        val storedHashedPassword = applicationContext.getSharedPreferences("com.upriseus.remindme", MODE_PRIVATE).getString(username, "")
        if (storedHashedPassword != null && storedHashedPassword != "")  {
            if(Hash.validatePassword(password, storedHashedPassword)){
                if (stay){
                    applicationContext.getSharedPreferences("com.upriseus.remindme", MODE_PRIVATE).edit().putInt("loginStatus", 1).apply()
                }else{
                    applicationContext.getSharedPreferences("com.upriseus.remindme", MODE_PRIVATE).edit().putInt("loginStatus", 0).apply()
                }
                val intent = Intent(applicationContext, HomeActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun createAccount(username: String, password: String){
        val hashedPassword = Hash.hash(password)
        applicationContext.getSharedPreferences("com.upriseus.remindme", MODE_PRIVATE).edit().putString(username, hashedPassword).apply()
        val intent = Intent(applicationContext, HomeActivity::class.java)
        startActivity(intent)
    }

}