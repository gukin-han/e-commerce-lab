package com.loopers.domain.rank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Rank {
    private final String member;
    private final Double score;
}
