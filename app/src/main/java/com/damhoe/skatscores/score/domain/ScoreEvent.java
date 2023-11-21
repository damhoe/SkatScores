package com.damhoe.skatscores.score.domain;

public class ScoreEvent {
   private SkatScore score;
   private ScoreEventType eventType;

   public ScoreEvent(SkatScore score, ScoreEventType eventType) {
      this.score = score;
      this.eventType = eventType;
   }

   public SkatScore getScore() {
      return score;
   }

   public void setScore(SkatScore score) {
      this.score = score;
   }

   public ScoreEventType getEventType() {
      return eventType;
   }

   public void setEventType(ScoreEventType eventType) {
      this.eventType = eventType;
   }
}
