package com.damhoe.skatscores

import android.os.Bundle
import android.os.Parcel
import com.damhoe.skatscores.score.domain.CreateScoreRequest
import com.damhoe.skatscores.score.domain.ScoreRequest
import org.junit.Test

/**
 * Tests for ScoreRequest class
 */
class ScoreRequestTests {
    @Test
    fun scoreRequest_shouldParcelAndUnparcelDerivedRequest() {

        val createRequest = CreateScoreRequest(
            gameId = 1234L,
            names = listOf("Alice", "Bob", "Christi"),
            positions = listOf(0, 1, 2)
        )

        // Write to parcel
        val bundle = Bundle()
        bundle.putParcelable("request", createRequest)

        // Unpack
        val unpackedRequest: ScoreRequest =
            bundle.getParcelable("request", ScoreRequest::class.java)
                ?: throw NullPointerException("Unpacked score request was null")

        // Check type and contents
        assert(unpackedRequest is CreateScoreRequest)
        val unpackedCreateRequest = unpackedRequest as CreateScoreRequest
        assert(unpackedCreateRequest.names.contains("Alice"))
        assert(unpackedCreateRequest.names.contains("Bob"))
        assert(unpackedCreateRequest.names.contains("Christi"))

    }
}