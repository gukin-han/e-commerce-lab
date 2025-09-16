package com.loopers.interfaces.api.rank;

import com.loopers.application.rank.RankFacade;
import com.loopers.application.rank.RankCommand;
import com.loopers.application.rank.RankResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.rank.RankV1Response.GetRankings;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankV1Controller implements RankV1ApiSpec {

    private final RankFacade rankFacade;

    @GetMapping
    @Override
    public ApiResponse<GetRankings> getRankings(RankV1Request.GetRankings request) {
        RankCommand.GetRankings command = new RankCommand.GetRankings(
                request.date(),
                request.size(),
                request.page()
        );

        RankResult.GetRankings result = rankFacade.getRankings(command);
        RankV1Response.GetRankings response = RankV1Response.GetRankings.from(result);

        return ApiResponse.success(response);
    }
}

