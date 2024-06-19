package com.acneappdetections

import android.os.Parcel
import android.os.Parcelable

data class DetectedAcne(
    val imageUri: String,
    val name: String,
    val type: String,
    val description: String,
    val treatment: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageUri)
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeString(description)
        parcel.writeString(treatment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DetectedAcne> {
        override fun createFromParcel(parcel: Parcel): DetectedAcne {
            return DetectedAcne(parcel)
        }

        override fun newArray(size: Int): Array<DetectedAcne?> {
            return arrayOfNulls(size)
        }
    }
}
