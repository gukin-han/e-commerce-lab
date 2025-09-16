package com.loopers.interfaces.api.rank;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Rank V1 API", description = "랭킹 조회 API")
public interface RankV1ApiSpec {

  @Operation(
      description = "상품 랭킹을 조회한다."
  )
  ApiResponse<RankV1Response.GetRankings> getRankings(
      @ParameterObject
      RankV1Request.GetRankings request
  );
}
