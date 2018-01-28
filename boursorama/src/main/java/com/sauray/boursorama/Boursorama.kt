package com.sauray.boursorama

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.sauray.boursorama.model.Account
import com.sauray.boursorama.model.LinearSVC
import com.sauray.boursorama.model.Movement
import com.sauray.boursorama.parser.parseAccounts
import com.sauray.boursorama.parser.parseMovements
import com.sauray.boursorama.utils.Optional
import com.sauray.boursorama.utils.getFeatures
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jsoup.Jsoup


/**
 * Created by Antoine Sauray on 25/01/2018.
 * The core class of the API
 */

class Boursorama private constructor(private val client : OkHttpClient) {

    companion object {
        private val TAG = "Boursorama"
        private var cookieJar : CookieJar? = null

        private fun loginWith(client: OkHttpClient, virtualKeyboardResponse: Response, formToken: String, login: String, password: String) : Boursorama? {
            virtualKeyboardResponse.body()?. let { response2Body ->

                /*
                    In Boursorama, a key (0,1,2 .. 9) is identified by a triple of letters
                    Each time we connect, the triple changes
                    As human, we click on images with the right number on it
                    In theory, only humans should be able to read the images
                    But we can use a HIGHLY ADVANCED ALGORITHM MADE OF ARTIFICIAL INTELLIGENCE
                    Just kidding, a logistic regression would do the job
                    I'm using a Linear Kernel SVM actually here (because someone made a nice port from Python to Java)
                 */

                // okay make a map with numbers (0, 1, 2 .. 9) to Triple (ABC-JEY-KEO ..)
                val keyboard = HashMap<Int, String>()

                // parse the virtual keyboard html content
                val parsedVirtualKeyboard = Jsoup.parse(response2Body.string())
                // take the Javascript (only one tag)
                val script = parsedVirtualKeyboard.getElementsByTag("script").html().toString()
                // split it with by double quotes (") and retrieve the third one
                val splits = script.split("\"")
                if(splits.size >= 3 ) {
                    // we now have the random matrix (that identifies the map we will build)
                    val formMatrixRandomChallenge = splits[3]

                    // make our classifier
                    val classifier = LinearSVC()
                    val passwordInput = parsedVirtualKeyboard.getElementsByClass("password-input")
                    // it is supposed to be a single element
                    if(passwordInput.size>0) {
                        passwordInput[0].children().forEach {
                            val child = it.child(0)
                            val key = child.attr("data-matrix-key")
                            val imgContent = child.attr("style").split(",")[1]
                            val decodedImg = Base64.decode(imgContent, Base64.DEFAULT)
                            val decodedByte = BitmapFactory.decodeByteArray(decodedImg, 0, decodedImg.size)
                            val width = decodedByte.width
                            val height = decodedByte.height / 2
                            val colorCropped = Bitmap.createBitmap(decodedByte, 0,0, width, height)
                            val features = getFeatures(colorCropped)
                            val number = classifier.predict(features)
                            keyboard[number] = key
                        }

                        // keyboard is initialized
                        // let's set the right password thanks to our key map
                        var code = ""
                        for(i in 0 until password.length) {
                            val number = password[i].toString().toInt()
                            if(i>0) {
                                code += "|"
                            }
                            code += keyboard[number]
                        }

                        // we now log in with our encrypted password
                        val loginResponse = client.newCall(postLoginRequest(login, password, code, formToken, formMatrixRandomChallenge)).execute()
                        if(loginResponse.isSuccessful) {
                            loginResponse.body()?.let {
                                val parsedLoginResponse = Jsoup.parse(it.string())
                                val lis = parsedLoginResponse.getElementsByTag("li")
                                var failed = false
                                for(li in lis) {
                                    Log.d(TAG, li.html())
                                    if(li.text().contains("Erreur d'authentification")) {
                                        failed = true
                                    }
                                }
                                if(!failed) return Boursorama(client)
                            }
                        }
                    }
                }
            }
            return null
        }

        fun getInstance(context: Context, login: String, password: String, success: (boursorama : Boursorama) -> Unit, failure: () -> Unit) {

            // if the cookie jar is already initialized, then it means we are connected
            if(cookieJar != null) {
                Log.d(TAG, "already authenticated")
                val client = OkHttpClient.Builder()//.addNetworkInterceptor(NetInterceptor())
                        .cookieJar(cookieJar).build()
                success(Boursorama(client))
            } else {
                Log.d(TAG, "need authentication")
                // otherwise we need to authenticate
                cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()

                Observable.fromCallable<Optional<Boursorama>> {


                    // the future instance
                    var ret: Boursorama? = null

                    val responseAccounts = client.newCall(getAccounts()).execute()
                    val accounts = parseAccounts(responseAccounts)
                    if (accounts != null) {
                        Log.d(TAG, "got accounts: " + accounts.toString())
                    } else {
                        Log.d(TAG, "needs new auth")
                        // first we get the login page
                        // we need to extract the form token for the authentication
                        val response = client.newCall(getLoginPage()).execute()

                        if (response.isSuccessful) {
                            // ensure the body is not null
                            response.body()?.let { responseBody ->
                                // parse the html content
                                val parsedHtml = Jsoup.parse(responseBody.string())
                                // retrieve the form token
                                val formToken = parsedHtml.getElementById("form__token").attr("value")

                                // request a virtual keyboard
                                val virtualKeyboardResponse = client.newCall(getVirtualKeyboard()).execute()
                                if (virtualKeyboardResponse.isSuccessful) {
                                    ret = loginWith(client, virtualKeyboardResponse, formToken, login, password)
                                } else {
                                    Log.d(TAG, "get keyboard result: " + virtualKeyboardResponse.code())
                                }
                            }
                        }
                    }
                    // if the client is not authenticated, clear the cookie
                    if (ret == null) cookieJar = null
                    Optional(ret)
            }.subscribeOn(Schedulers.computation()).subscribe { optionalBoursorama ->
                Log.d(TAG, "Finished !")
                optionalBoursorama.get()?. let { success(it) } ?: failure()
                }
            }
        }
    }


    /**
     * Get bank accounts
     */
    fun getAccounts(success: (accounts: List<Account>) -> Unit, failure: () -> Unit) {
        Observable.fromCallable<Optional<List<Account>>> {
            val response = client.newCall(getAccounts()).execute()
            val accounts = parseAccounts(response)
            Optional(accounts)
        }.subscribeOn(Schedulers.computation()).subscribe { it.get()?. let { success(it) } ?: failure() }
    }

    /**
     * Get movements associated to a bank account
     * @see getAccounts
     */
    fun getMovements(account: Account, success: (accounts: List<Movement>) -> Unit, failure: () -> Unit) {
        Observable.fromCallable<Optional<List<Movement>>> {
            val response = client.newCall(getMovementsRequest(account)).execute()
            val movements = parseMovements(response)
            Optional(movements)
        }.subscribeOn(Schedulers.computation()).subscribe{ it.get()?. let { success(it) } ?: failure() }
    }
}
