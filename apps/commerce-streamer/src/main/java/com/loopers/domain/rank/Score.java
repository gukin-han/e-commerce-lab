package com.loopers.domain.rank;

import lombok.Getter;

@Getter
public class Score {

  private int viewDelta;
  private int likeDelta;
  private int orderDelta;

  private double score;

  private Score(int viewDelta, int likeDelta, int orderDelta) {
    this.viewDelta = viewDelta;
    this.likeDelta = likeDelta;
    this.orderDelta = orderDelta;

    this.score = this.calculateScore(viewDelta, likeDelta, orderDelta);
  }

  private double calculateScore(int viewDelta, int likeDelta, int orderDelta) {

    double score = viewDelta * Weight.VIEW.getWeight()
        + likeDelta * Weight.LIKE.getWeight()
        + orderDelta * Weight.ORDER.getWeight();

    if (score == 0.0 || Double.isNaN(score) || Double.isInfinite(score)) {
      throw new IllegalStateException("Invalid Score : " + score);
    }

    return score;
  }

  public static Score create(int viewCount, int likeCount, int orderCount) {
    return new Score(viewCount, likeCount, orderCount);
  }
}
