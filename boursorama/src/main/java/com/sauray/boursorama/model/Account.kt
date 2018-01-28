package com.sauray.boursorama.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

/**
 * Created by Antoine Sauray on 28/01/2018.
 * A Bank account
 */
class Account (val name: String, val href: String) : Parcelable {


    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        if (p0 != null) {
            p0.writeString(name)
            p0.writeString(href)
        }
    }

    override fun toString() : String {
        return name
    }

    companion object CREATOR : Parcelable.Creator<Account> {
        override fun createFromParcel(parcel: Parcel): Account {
            return Account(parcel)
        }

        override fun newArray(size: Int): Array<Account?> {
            return arrayOfNulls(size)
        }
    }
}