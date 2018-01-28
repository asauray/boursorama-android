package com.sauray.boursorama.parser

import android.util.Log
import com.sauray.boursorama.model.Account
import com.sauray.boursorama.model.Movement
import com.sauray.boursorama.utils.saveString
import okhttp3.Response
import org.jsoup.Jsoup

/**
 * Created by Antoine Sauray on 28/01/2018.
 * Parser for the movements query
 */

fun parseMovements(response: Response) : List<Movement>? {

    response.body()?. let {
        val movements = ArrayList<Movement>()
        val body = it.string()
        val html = Jsoup.parse(body)
        val movementsHtml = html.getElementsByClass("list__movement__line list__movement__line--even ")
        movementsHtml.forEach {
            val date = it.attr("data-operations-item-date")
            val parsedChild = Jsoup.parse(it.children().html())
            val amount = parsedChild.getElementsByClass("list__movement__line--amount").text()
            movements.add(Movement(amount, date))
        }
        return movements
    } ?: return null
}