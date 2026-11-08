package com.damhoe.skatscores.persistence

import android.database.sqlite.SQLiteDatabase
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncMetaTable
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hybrid logical clock for ordering rounds. A seq is `<13-digit millis>-<4-digit counter>-<device>`;
 * the fixed widths make text order equal time order, and the device id keeps rounds written on
 * different devices from colliding.
 */
@Singleton
class RoundSequence @Inject constructor()
{
    internal var clock: () -> Long = System::currentTimeMillis

    private var lastMillis = 0L
    private var lastCounter = 0

    @Volatile
    private var deviceId: String? = null

    /**
     * A seq sorting after [after] and after every seq handed out before, even if the clock went
     * backwards. Call inside the insert transaction, with [after] read in that transaction.
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
        /** Device part of seqs derived from pre-seq round numbers; see [Migrations]. */
        const val LEGACY_DEVICE = "legacy"

        private const val MAX_COUNTER = 9999

        fun format(millis: Long, counter: Int, device: String): String =
            "%013d-%04d-%s".format(Locale.ROOT, millis, counter, device)

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
