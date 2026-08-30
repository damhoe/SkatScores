package com.damhoe.skatscores.persistence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The scripts in assets/tables are only as good as this splitter: everything it fails to hand
 * to execSQL separately never reaches the database at all.
 */
class SqlScriptsTest
{
    @Test
    fun `a single statement comes back without its semicolon`()
    {
        val statements = splitSqlStatements(
            """
            CREATE TABLE IF NOT EXISTS players (
                id VARCHAR(36) PRIMARY KEY
            );
            """.trimIndent()
        )

        assertEquals(1, statements.size)
        assertTrue(statements[0].startsWith("CREATE TABLE"))
        assertTrue(statements[0].endsWith(")"))
    }

    @Test
    fun `a table and the index after it are two statements`()
    {
        // This is the case that used to be lost: only the first statement was executed, so
        // the index was never created.
        val statements = splitSqlStatements(
            """
            CREATE TABLE IF NOT EXISTS seats (
                game_id VARCHAR(36) NOT NULL,
                player_id VARCHAR(36) NULL
            );

            CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_player
            ON seats(game_id, player_id)
            WHERE player_id IS NOT NULL;
            """.trimIndent()
        )

        assertEquals(2, statements.size)
        assertTrue(statements[1].startsWith("CREATE UNIQUE INDEX"))
        assertTrue(statements[1].endsWith("WHERE player_id IS NOT NULL"))
    }

    @Test
    fun `a trigger body is not cut at its inner semicolons`()
    {
        val statements = splitSqlStatements(
            """
            CREATE TABLE IF NOT EXISTS games (
                id VARCHAR(36) PRIMARY KEY
            );

            CREATE TRIGGER IF NOT EXISTS update_games_updated_at
            AFTER UPDATE ON games
            FOR EACH ROW
            BEGIN
                UPDATE games SET updated_at = 'now' WHERE id = NEW.id;
            END;
            """.trimIndent()
        )

        assertEquals(2, statements.size)

        val trigger = statements[1]
        assertTrue(trigger.startsWith("CREATE TRIGGER"))
        // The statement inside the body keeps its own semicolon; only the trigger's own
        // terminator is stripped.
        assertTrue(trigger.contains("WHERE id = NEW.id;"))
        assertTrue(trigger.endsWith("END"))
    }

    @Test
    fun `comments and blank lines are dropped`()
    {
        val statements = splitSqlStatements(
            """
            -- A leading comment.

            CREATE TABLE t (id INT);

            -- A trailing comment.
            """.trimIndent()
        )

        assertEquals(listOf("CREATE TABLE t (id INT)"), statements)
    }

    @Test
    fun `a statement without a trailing semicolon still counts`()
    {
        assertEquals(listOf("CREATE TABLE t (id INT)"), splitSqlStatements("CREATE TABLE t (id INT)"))
    }

    @Test
    fun `an empty script yields nothing`()
    {
        assertEquals(emptyList<String>(), splitSqlStatements("\n\n-- nothing here\n"))
    }
}
