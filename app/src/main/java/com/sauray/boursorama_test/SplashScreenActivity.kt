package com.sauray.boursorama_test

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.sauray.boursorama.Boursorama
import com.sauray.boursorama.model.Account
import android.app.ProgressDialog
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.inputmethod.InputMethodManager


/**
 * Splash screen activity, decides what to do next
 */
class SplashScreenActivity : AppCompatActivity() {

    private val TAG = "SplashScreenActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val auth = Authentication(this)
        val credentials = auth.getCredentials(this)
        if(credentials != null) {
            authenticate(credentials.first, credentials.second)
        } else {
            val identifierEditText = findViewById<EditText>(R.id.identifier)
            val passwordEditText = findViewById<EditText>(R.id.password)
            val loginButton = findViewById<Button>(R.id.login)
            loginButton.setOnClickListener({
                val identifier = identifierEditText.text.toString()
                val password = passwordEditText.text.toString()
                runOnUiThread({
                    authenticate(identifier, password)
                })
            })
        }
    }

    fun authenticate(identifier: String, password: String) {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        val pd = ProgressDialog(this)
        pd.setMessage(getString(R.string.checking_credentials))
        pd.show()

        Boursorama.getInstance(this, identifier, password, {

            Authentication(this).saveCredentials(this, identifier, password)

            it.getAccounts({
                val bankAccounts = ArrayList<Account>()
                for(account in it) {
                    bankAccounts.add(Account(account.name, account.href))
                }
                Log.d(TAG, "accounts: "+bankAccounts.toString())
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(getString(R.string.accounts_key), bankAccounts)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                runOnUiThread({
                    pd.dismiss()
                    startActivity(intent)
                })
            }, {
                runOnUiThread({
                    pd.dismiss()
                    Snackbar.make(findViewById<CoordinatorLayout>(R.id.coordinatorLayout), R.string.credentials_incorrect, Snackbar.LENGTH_LONG).show()
                })
            })
        }, {
            runOnUiThread({
                pd.dismiss()
                Snackbar.make(findViewById<CoordinatorLayout>(R.id.coordinatorLayout), R.string.credentials_incorrect, Snackbar.LENGTH_LONG).show()
            })

        })
    }
}
