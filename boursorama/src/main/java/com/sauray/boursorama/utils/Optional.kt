package com.sauray.boursorama.utils

/**
 * Created by Antoine Sauray on 25/01/2018.
 */
class Optional<M>(private val optional: M?) {

    fun get(): M? {
        return optional
    }
}