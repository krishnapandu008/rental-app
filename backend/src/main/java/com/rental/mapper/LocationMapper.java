package com.rental.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.rental.dto.LocationResponseDto;
import com.rental.entity.Location;
import com.rental.mapper.base.BaseMapper;

/**
 * Mapper for Location entity ↔ LocationResponseDto
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LocationMapper extends BaseMapper<Location, LocationResponseDto> {

    @Named("toBasicDto")
    LocationResponseDto toBasicDto(Location location);
}