package com.loopers.domain.brand;

import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BrandService {

    private final BrandRepository brandRepository;

    public Brand findByBrandId(Long brandId) {
        return brandRepository.findById(brandId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                String.format("브랜드 ID %s에 해당하는 브랜드를 찾을 수 없습니다.", brandId)
        ));
    }

    public List<Brand> findAllByIds(List<Long> brandIds) {
        return brandRepository.findAllByBrandIdIn(brandIds);
    }
}
