package com.rental.mapper;

import com.rental.dto.ImageResponseDto;
import com.rental.entity.PropertyImage;
import com.rental.mapper.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for PropertyImage entity ↔ ImageResponseDto
 * Auto-maps fields with same names
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PropertyImageMapper extends BaseMapper<PropertyImage, ImageResponseDto> {
    // No methods needed - inherits from BaseMapper
    // MapStruct will auto-generate all methods
}