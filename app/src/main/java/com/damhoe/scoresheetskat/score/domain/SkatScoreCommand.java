package com.damhoe.scoresheetskat.score.domain;

public class SkatScoreCommand {
    private int spitzen;
    private SkatSuit suit; // Clubs, Diamonds, Hears, Spades, Null, Grand, Overbid
    private boolean hand;
    private boolean schneider;
    private boolean schneiderAnnounced;
    private boolean schwarz;
    private boolean schwarzAnnounced;
    private boolean ouvert;
    private boolean isWon;
    private int playerPosition;
    private long gameId;
    private int index;
    private boolean isPasse;

    public SkatScoreCommand() {
        spitzen = 1;
        suit = SkatSuit.CLUBS;
        hand = false;
        ouvert = false;
        schneider = false;
        schneiderAnnounced = false;
        schwarz = false;
        schwarzAnnounced = false;
        isWon = true;
        gameId = -1L;
        playerPosition = 0;
        index = 0;
        isPasse = false;
    }

    public static SkatScoreCommand fromSkatScore(SkatScore score) {
        SkatScoreCommand command = new SkatScoreCommand();
        command.setHand(score.isHand());
        command.setOuvert(score.isOuvert());
        command.setWon(score.isWon());
        command.setSchneider(score.isSchneider());
        command.setSchwarz(score.isSchwarz());
        command.setSuit(score.getSuit());
        command.setSpitzen(score.getSpitzen());
        command.setSchneiderAnnounced(score.isSchneiderAnnounced());
        command.setSchwarzAnnounced(score.isSchwarzAnnounced());
        command.setPlayerPosition(score.getPlayerPosition());
        command.setGameId(score.getGameId());
        command.setIndex(score.getIndex());
        command.setPasse(score.isPasse());
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

    public boolean isWon() {
        return isWon;
    }

    public void setWon(boolean won) {
        isWon = won;
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

    public boolean isSuitsEnabled() {
        return !isPasse;
    }

    public boolean isSpitzenEnabled() {
        return !isPasse && suit != SkatSuit.NULL;
    }

    public boolean isSchneiderSchwarzEnabled() {
        return suit != SkatSuit.NULL && !isPasse;
    }

    public boolean isOuvertEnabled() {
        return !isPasse;
    }

    public boolean isHandEnabled() {
        return !isPasse;
    }

    public boolean isResultEnabled() {
        return !isPasse;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isPasse() {
        return isPasse;
    }

    public void setPasse(boolean passe) {
        isPasse = passe;
    }
}
