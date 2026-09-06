package com.rental.mapper;

import com.rental.dto.PropertyTypeResponseDto;
import com.rental.entity.PropertyType;
import com.rental.mapper.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for PropertyType entity ↔ PropertyTypeResponseDto
 * Auto-maps fields with same names
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PropertyTypeMapper extends BaseMapper<PropertyType, PropertyTypeResponseDto> {
    // No methods needed - inherits from BaseMapper
    // MapStruct will auto-generate all methods
}