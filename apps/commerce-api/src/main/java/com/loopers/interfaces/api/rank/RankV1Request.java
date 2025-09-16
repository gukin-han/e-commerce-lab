package com.loopers.interfaces.api.rank;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class RankV1Request {

    @Builder
    public record GetRankings(
            @DateTimeFormat(pattern = "yyyyMMdd")
            LocalDate date,
            int size,
            int page
    ) {
    }
}