package com.damhoe.scoresheetskat.base;

import java.util.List;

public class SearchResult<T> {
   public final int numberOfHits;
   public final List<T> hits;

   public SearchResult(int numberOfHits, List<T> hits) {
      this.hits = hits;
      this.numberOfHits = numberOfHits;
   }

   public static class Builder<T> {
      private List<T> hits;

      public Builder<T> hits(List<T> hits) {
         this.hits = hits;
         return this;
      }

      public SearchResult<T> build() {
         if (hits == null) {
            return null;
         }
         int numberOfHits = hits.size();
         return new SearchResult<T>(numberOfHits, hits);
      }
   }
}
