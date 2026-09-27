package com.damhoe.skatscores.persistence

import android.database.sqlite.SQLiteDatabase
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncMetaTable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hands out the `seq` a new round is ordered by: a hybrid logical clock.
 *
 * A seq is `<13-digit wall-clock millis>-<4-digit counter>-<device id>`. The fields have fixed
 * widths, so comparing two seqs as text compares them in time order, which is what
 * `ORDER BY seq` relies on. Rounds are then numbered by their position, not stored with one.
 *
 * Why not just the next round number:
 * - Removing a round no longer renumbers every round after it.
 * - Two devices adding a round to the same list while apart would both claim number N. Two
 *   seqs never collide - the device id breaks the tie - so the rounds merge in time order.
 *
 * A new seq always sorts after the newest round already in the list ([next]'s `after`), even
 * if this device's clock has gone backwards since, so a round played now never lands in the
 * middle of the list.
 */
@Singleton
class RoundSequence @Inject constructor()
{
    /** Replaceable so tests can pin the time. */
    internal var clock: () -> Long = System::currentTimeMillis

    private var lastMillis = 0L
    private var lastCounter = 0

    @Volatile
    private var deviceId: String? = null

    /**
     * The seq for a round written now, sorting after [after] and after every seq this device
     * handed out before. Call it inside the transaction that inserts the round, with [after]
     * read in the same transaction, so nothing can slip in between.
     */
    @Synchronized
    fun next(db: SQLiteDatabase, after: String?): String
    {
        val (afterMillis, afterCounter) = after?.let(::parse) ?: (0L to -1)

        var millis = maxOf(clock(), lastMillis, afterMillis)
        var counter = maxOf(
            if (millis == lastMillis) lastCounter + 1 else 0,
            if (millis == afterMillis) afterCounter + 1 else 0,
        )

        if (counter > MAX_COUNTER)
        {
            millis += 1
            counter = 0
        }

        lastMillis = millis
        lastCounter = counter

        return format(millis, counter, deviceId(db))
    }

    private fun deviceId(db: SQLiteDatabase): String
    {
        deviceId?.let { return it }

        val stored = readDeviceId(db) ?: run {
            Schema.seedDeviceId(db)
            readDeviceId(db)
        } ?: throw IllegalStateException("No device id in ${SyncMetaTable.TABLE_NAME}")

        return stored.also { deviceId = it }
    }

    private fun readDeviceId(db: SQLiteDatabase): String? =
        db.rawQuery(
            "SELECT ${SyncMetaTable.COLUMN_VALUE} FROM ${SyncMetaTable.TABLE_NAME}" +
                    " WHERE ${SyncMetaTable.COLUMN_KEY} = ?",
            arrayOf(SyncMetaTable.KEY_DEVICE_ID),
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

    companion object
    {
        /** What rounds written before seqs existed carry as their device; see [Migrations]. */
        const val LEGACY_DEVICE = "legacy"

        private const val MAX_COUNTER = 9999

        fun format(millis: Long, counter: Int, device: String): String =
            "%013d-%04d-%s".format(java.util.Locale.ROOT, millis, counter, device)

        /** Millis and counter of a seq; the device id is only a tie-breaker. */
        fun parse(seq: String): Pair<Long, Int>?
        {
            val parts = seq.split('-', limit = 3)
            if (parts.size < 2) return null
            val millis = parts[0].toLongOrNull() ?: return null
            val counter = parts[1].toIntOrNull() ?: return null
            return millis to counter
        }
    }
}
