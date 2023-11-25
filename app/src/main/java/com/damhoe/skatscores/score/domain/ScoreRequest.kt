package com.damhoe.skatscores.score.domain

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ScoreRequest {
    abstract val names: List<String>
    abstract val positions: List<Int>
}

data class CreateScoreRequest(
    override val names: List<String>,
    override val positions: List<Int>,
    val gameId: Long
) : ScoreRequest(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList()?: emptyList(),
        parcel.createIntArray()?.toList()?: emptyList(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(names)
        parcel.writeLong(gameId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CreateScoreRequest> {
        override fun createFromParcel(parcel: Parcel): CreateScoreRequest {
            return CreateScoreRequest(parcel)
        }

        override fun newArray(size: Int): Array<CreateScoreRequest?> {
            return arrayOfNulls(size)
        }
    }
}

data class UpdateScoreRequest(
    override val names: List<String>,
    override val positions: List<Int>,
    val scoreId: Long
) : ScoreRequest(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList()?: emptyList(),
        parcel.createIntArray()?.toList()?: emptyList(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(names)
        parcel.writeLong(scoreId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpdateScoreRequest> {
        override fun createFromParcel(parcel: Parcel): UpdateScoreRequest {
            return UpdateScoreRequest(parcel)
        }

        override fun newArray(size: Int): Array<UpdateScoreRequest?> {
            return arrayOfNulls(size)
        }
    }
}