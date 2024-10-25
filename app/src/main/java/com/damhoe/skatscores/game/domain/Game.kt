package com.damhoe.skatscores.game.domain

import com.damhoe.skatscores.game.domain.score.Score
import com.damhoe.skatscores.player.domain.Player
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.UUID

abstract class Game<TSettings : GameSettings, TScore : Score>(
    var title: String,
    protected var players: MutableList<Player>,
    var settings: TSettings)
{
    enum class RunState
    {
        RUNNING,
        FINISHED
    }
    
    var id: Long = UUID.randomUUID().mostSignificantBits
    var createdAt: Date = Calendar.getInstance().time
    protected var runState: RunState? = null
    protected var scores: MutableList<TScore> = ArrayList()
    
    init
    {
        start()
    }
    
    open fun addScore(score: TScore)
    {
        scores.add(score)
    }
    
    open fun removeLastScore(): TScore?
    {
        val lastIndex = scores.size - 1
        if (lastIndex < 0)
        {
            return null
        }
        return scores.removeAt(lastIndex)
    }
    
    fun updateScore(score: TScore)
    {
        for (i in scores.indices)
        {
            if (scores[i]!!.id == score!!.id)
            {
                scores[i] = score
                break
            }
        }
    }
    
    fun getScores(): List<TScore>
    {
        return scores
    }
    
    open fun start()
    {
        runState = RunState.RUNNING
    }
    
    open fun finish()
    {
        runState = RunState.FINISHED
    }
    
    fun resume()
    {
        runState = RunState.RUNNING
    }
    
    val isFinished: Boolean
        get() = runState == RunState.FINISHED
    
    fun getPlayers(): Array<Player>
    {
        return Collections.unmodifiableList(players)
    }
    
    fun setPlayers(players: List<Player>)
    {
        this.players = players
    }
    
    abstract class Builder<
            T : Game<S, C>?,
            S : GameSettings?, C : Score?>
    {
        @JvmField var mTitle: String? = null
        @JvmField var mPlayers: MutableList<Player>? = null;
        @JvmField var mSettings: S? = null
        
        abstract fun build(): T
        
        fun fromCommand(command: GameCommand<S>): Builder<T, S, C>
        {
            mTitle = command.getTitle()
            mPlayers = ArrayList()
            for (j in 1..command.getNumberOfPlayers())
            {
                mPlayers?.add(Player("Player $j"))
            }
            mPlayers = Collections.unmodifiableList(mPlayers)
            mSettings = command.getSettings()
            return this
        }
        
        fun setSettings(settings: S): Builder<T, S, C>
        {
            mSettings = settings
            return this
        }
        
        fun setTitle(title: String?): Builder<T, S, C>
        {
            mTitle = title
            return this
        }
        
        fun setPlayers(players: MutableList<Player>?): Builder<T, S, C>
        {
            mPlayers = players
            return this
        }
    }
}
