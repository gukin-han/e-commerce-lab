package com.loopers.domain.rank;

public enum Weight {
  VIEW(0.1), LIKE(0.2), ORDER(0.7);

  private final double weight;

  Weight(double weight) {
    this.weight = weight;
  }

  public double getWeight() {
    return weight;
  }
}
