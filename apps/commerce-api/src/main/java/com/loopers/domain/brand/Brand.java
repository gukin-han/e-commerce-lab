package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "brands")
@Entity
public class Brand extends BaseEntity {

    private String name;

    @Builder
    private Brand(String name) {
        this.name = name;
    }

    public static Brand create(String name) {
        return Brand.builder()
                .name(name)
                .build();
    }
}
