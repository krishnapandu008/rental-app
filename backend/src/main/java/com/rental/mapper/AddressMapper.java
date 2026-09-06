package com.rental.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rental.dto.AddressResponseDto;
import com.rental.entity.Address;
import com.rental.mapper.base.BaseMapper;

/**
 * Mapper for Address entity ↔ AddressResponseDto
 * Auto-maps fields with same names
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = { LocationMapper.class }
)
public interface AddressMapper extends BaseMapper<Address, AddressResponseDto> {
    // No methods needed - inherits from BaseMapper
}