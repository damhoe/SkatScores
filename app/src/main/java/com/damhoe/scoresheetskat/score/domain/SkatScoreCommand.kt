package com.damhoe.scoresheetskat.score.domain;

import com.damhoe.scoresheetskat.score.Constant;

public class SkatScoreCommand {
    private int spitzen;
    private SkatSuit suit; // Clubs, Diamonds, Hears, Spades, Null, Grand
    private SkatResult result; // Won, Lost, Overbid, Passe
    private boolean hand;
    private boolean schneider;
    private boolean schneiderAnnounced;
    private boolean schwarz;
    private boolean schwarzAnnounced;
    private boolean ouvert;
    private int playerPosition;
    private long gameId;

    public SkatScoreCommand() {
        spitzen = 1;
        suit = SkatSuit.INVALID;
        result = SkatResult.PASSE;
        hand = false;
        ouvert = false;
        schneider = false;
        schneiderAnnounced = false;
        schwarz = false;
        schwarzAnnounced = false;
        gameId = -1L;
        playerPosition = Constant.PASSE_PLAYER_POSITION;
    }

    public static SkatScoreCommand fromSkatScore(SkatScore score) {
        SkatScoreCommand command = new SkatScoreCommand();
        command.setHand(score.isHand());
        command.setOuvert(score.isOuvert());
        command.setResult(score.getResult());
        command.setSchneider(score.isSchneider());
        command.setSchwarz(score.isSchwarz());
        command.setSuit(score.getSuit());
        command.setSpitzen(score.getSpitzen());
        command.setSchneiderAnnounced(score.isSchneiderAnnounced());
        command.setSchwarzAnnounced(score.isSchwarzAnnounced());
        command.setPlayerPosition(score.getPlayerPosition());
        command.setGameId(score.getGameId());
        return command;
    }

    public boolean isValid() {
        // TODO
        return true;
    }

    public void setSuit(SkatSuit suit) {
        this.suit = suit;
    }

    public SkatSuit getSuit() {
        return suit;
    }

    public boolean isHand() {
        return hand;
    }

    public void setHand(boolean hand) {
        this.hand = hand;
    }

    public boolean isSchneider() {
        return schneider;
    }

    public void setSchneider(boolean schneider) {
        this.schneider = schneider;
    }

    public boolean isSchwarz() {
        return schwarz;
    }

    public void setSchwarz(boolean schwarz) {
        this.schwarz = schwarz;
    }

    public boolean isOuvert() {
        return ouvert;
    }

    public void setOuvert(boolean ouvert) {
        this.ouvert = ouvert;
    }

    public SkatResult getResult() {
        return result;
    }

    public void setResult(SkatResult result) {
        this.result = result;
    }

    public int getSpitzen() {
        return spitzen;
    }

    public void setSpitzen(int spitzen) {
        this.spitzen = spitzen;
    }

    public boolean trySetSpitzen(int spitzen) {
        if (spitzen < getMinSpitzen() || spitzen > getMaxSpitzen()) {
            return false;
        }
        setSpitzen(spitzen);
        return true;
    }

    public boolean isSchneiderAnnounced() {
        return schneiderAnnounced;
    }

    public void setSchneiderAnnounced(boolean schneiderAnnounced) {
        this.schneiderAnnounced = schneiderAnnounced;
    }

    public boolean isSchwarzAnnounced() {
        return schwarzAnnounced;
    }

    public void setSchwarzAnnounced(boolean schwarzAnnounced) {
        this.schwarzAnnounced = schwarzAnnounced;
    }

    public boolean isMinSpitzen() {
        return spitzen <= getMinSpitzen();
    }

    public boolean isMaxSpitzen() {
        return spitzen >= getMaxSpitzen();
    }

    private int getMinSpitzen() {
        return 1;
    }

    private int getMaxSpitzen() {
        if (suit == SkatSuit.GRAND) {
            return 4;
        }
        return 11;
    }

    public int getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(int playerPosition) {
        this.playerPosition = playerPosition;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public boolean isPasse() { return playerPosition == Constant.PASSE_PLAYER_POSITION; }

    public boolean isWon() { return SkatResult.WON.equals(getResult()); }

    public boolean isOverbid() { return SkatResult.OVERBID.equals(getResult()); }

    public boolean isLost() {
        return SkatResult.LOST.equals(getResult());
    }

    public void resetWinLevels() {
        setHand(false);
        setOuvert(false);
        setSchwarz(false);
        setSchwarzAnnounced(false);
        setSchneider(false);
        setSchneiderAnnounced(false);
    }

    public void resetSpitzen() {
        setSpitzen(1);
    }
}
