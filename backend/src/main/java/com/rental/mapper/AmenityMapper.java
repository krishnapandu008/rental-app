package com.rental.mapper;

import com.rental.dto.AmenityResponseDto;
import com.rental.entity.Amenity;
import com.rental.mapper.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Amenity entity ↔ AmenityResponseDto
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AmenityMapper extends BaseMapper<Amenity, AmenityResponseDto> {
    // No methods needed - inherits from BaseMapper
}