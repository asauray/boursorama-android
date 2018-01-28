package com.sauray.boursorama

import org.junit.Test

/**
 * Created by Antoine Sauray on 25/01/2018.
 */
class LoginAndroidTest {

    @Test
    fun testLogin() {
        Boursorama.getInstance("", "", { success, _ ->
            System.out.println("finished : success = " + success)
        })
    }
}