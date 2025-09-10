package com.loopers.application.rank;

import java.time.LocalDate;

public class RankCommand {
    public record GetRankings(
            LocalDate date,
            int size,
            int page
    ) {
    }
}
