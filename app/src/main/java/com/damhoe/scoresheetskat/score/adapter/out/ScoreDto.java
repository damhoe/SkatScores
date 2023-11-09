package com.damhoe.scoresheetskat.score.adapter.out;

import com.damhoe.scoresheetskat.score.domain.SkatResult;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.score.domain.SkatScoreCommand;
import com.damhoe.scoresheetskat.score.domain.SkatSuit;

class ScoreDto {
   // General properties
   private long id;
   private long gameId;
   private int playerPosition;
   // Skat specific properties
   private int spitzen; // negative value if missing
   private int suit; // Clubs, Diamonds, Hears, Spades, Null, Grand
   private int hand;
   private int schneider;
   private int schneiderAnnounced;
   private int schwarz;
   private int schwarzAnnounced;
   private int ouvert;
   private int result;

   public SkatScore toScore() {
      SkatScoreCommand skatScoreCommand = new SkatScoreCommand();
      skatScoreCommand.setGameId( getGameId() );
      skatScoreCommand.setPlayerPosition( getPlayerPosition() );
      skatScoreCommand.setSpitzen( getSpitzen() );
      skatScoreCommand.setSuit( SkatSuit.fromInteger( getSuit() ));
      skatScoreCommand.setHand( getHand() == 1 );
      skatScoreCommand.setSchneider( getSchneider() == 1 );
      skatScoreCommand.setSchneiderAnnounced( getSchneiderAnnounced() == 1 );
      skatScoreCommand.setSchwarz( getSchwarz() == 1 );
      skatScoreCommand.setSchwarzAnnounced( getSchwarzAnnounced() == 1 );
      skatScoreCommand.setOuvert( getOuvert() ==1 );
      skatScoreCommand.setResult( SkatResult.fromInteger(getResult()) );

      SkatScore skatScore = new SkatScore(skatScoreCommand);
      skatScore.setId(id);
      return skatScore;
   }

   public static ScoreDto fromScore(SkatScore skatScore) {
      ScoreDto scoreDto = new ScoreDto();
      scoreDto.setId( skatScore.getId() );
      scoreDto.setGameId( skatScore.getGameId() );
      scoreDto.setPlayerPosition( skatScore.getPlayerPosition() );
      scoreDto.setSpitzen( skatScore.getSpitzen() );
      scoreDto.setSuit( skatScore.getSuit().asInteger() );
      scoreDto.setHand( skatScore.isHand() ? 1 : 0 );
      scoreDto.setSchneider( skatScore.isSchneider() ? 1 : 0 );
      scoreDto.setSchneiderAnnounced( skatScore.isSchneiderAnnounced() ? 1 : 0 );
      scoreDto.setSchwarz( skatScore.isSchwarz() ? 1 : 0 );
      scoreDto.setSchwarzAnnounced( skatScore.isSchwarzAnnounced() ? 1 : 0 );
      scoreDto.setOuvert( skatScore.isOuvert() ? 1 : 0 );
      scoreDto.setResult( skatScore.getResult().asInteger() );
      return scoreDto;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public long getGameId() {
      return gameId;
   }

   public void setGameId(long gameId) {
      this.gameId = gameId;
   }

   public int getPlayerPosition() {
      return playerPosition;
   }

   public void setPlayerPosition(int playerPosition) {
      this.playerPosition = playerPosition;
   }

   public int getSpitzen() {
      return spitzen;
   }

   public void setSpitzen(int spitzen) {
      this.spitzen = spitzen;
   }

   public int getSuit() {
      return suit;
   }

   public void setSuit(int suit) {
      this.suit = suit;
   }

   public int getHand() {
      return hand;
   }

   public void setHand(int hand) {
      this.hand = hand;
   }

   public int getSchneider() {
      return schneider;
   }

   public void setSchneider(int schneider) {
      this.schneider = schneider;
   }

   public int getSchneiderAnnounced() {
      return schneiderAnnounced;
   }

   public void setSchneiderAnnounced(int schneiderAnnounced) {
      this.schneiderAnnounced = schneiderAnnounced;
   }

   public int getSchwarz() {
      return schwarz;
   }

   public void setSchwarz(int schwarz) {
      this.schwarz = schwarz;
   }

   public int getSchwarzAnnounced() {
      return schwarzAnnounced;
   }

   public void setSchwarzAnnounced(int schwarzAnnounced) {
      this.schwarzAnnounced = schwarzAnnounced;
   }

   public int getOuvert() {
      return ouvert;
   }

   public void setOuvert(int ouvert) {
      this.ouvert = ouvert;
   }

   public int getResult() {
      return result;
   }

   public void setResult(int isWon) {
      this.result = isWon;
   }
}
