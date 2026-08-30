package com.damhoe.skatscores.persistence

/**
 * Splits a SQL script into executable statements.
 *
 * `execSQL` runs one statement, so a script has to be handed to it a statement at a time or
 * everything after the first one is silently dropped - which is how the triggers and indexes
 * at the end of these scripts came to be missing.
 *
 * A plain split on ";" would cut a trigger in half, because the statements inside its
 * BEGIN ... END body end in semicolons of their own; those only end the trigger when END
 * follows. Comment lines and blank lines are dropped.
 */
fun splitSqlStatements(script: String): List<String>
{
    val statements = mutableListOf<String>()
    val current = StringBuilder()
    var insideTriggerBody = false

    fun takeStatement()
    {
        current.toString().trim().trimEnd(';').takeIf { it.isNotEmpty() }
            ?.let { statements += it }
        current.setLength(0)
    }

    script.lineSequence().forEach { rawLine ->
        val line = rawLine.trim()
        if (line.isEmpty() || line.startsWith("--")) return@forEach

        current.append(line).append('\n')

        val upper = line.uppercase()
        when
        {
            upper == "BEGIN" || upper.endsWith(" BEGIN") -> insideTriggerBody = true

            insideTriggerBody && upper.startsWith("END") ->
            {
                insideTriggerBody = false
                takeStatement()
            }

            !insideTriggerBody && line.endsWith(";") -> takeStatement()
        }
    }

    // A trailing statement without its semicolon still counts.
    takeStatement()

    return statements
}
