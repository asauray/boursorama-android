package com.sauray.boursorama

import android.util.Log
import org.junit.Test

/**
 * Created by Antoine Sauray on 25/01/2018.
 */
class LoginUnitTest {

    @Test
    fun verifyLogin() {
        Log.d("MainActivity", "Bonjour")
        com.sauray.boursorama.Boursorama.getInstance("", "", { success, _ ->
            Log.d("MainActivity", "finished : success = " + success)
        })
        Log.d("MainActivity", "Au revoir")
    }
}