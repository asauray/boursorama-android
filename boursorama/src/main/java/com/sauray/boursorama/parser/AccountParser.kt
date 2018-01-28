package com.sauray.boursorama.parser

import android.util.Log
import com.sauray.boursorama.model.Account
import okhttp3.Response
import org.jsoup.Jsoup

/**
 * Created by Antoine Sauray on 28/01/2018.
 * Parser for the account query
 */

fun parseAccounts(response: Response) : List<Account>? {

    response.body()?. let {
        var accounts : ArrayList<Account>? = null
        val body = it.string()
        val html = Jsoup.parse(body)
        val accountsHtml = html.getElementsByClass("account--name")
        if(accountsHtml.size > 0) {
            accounts = ArrayList()
            accountsHtml.forEach {
                accounts.add(Account(it.text(), it.attr("href")))
            }
        }
        return accounts
    } ?: return null
}