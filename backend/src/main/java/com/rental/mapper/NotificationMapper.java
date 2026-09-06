package com.rental.mapper;

import com.rental.dto.NotificationResponseDto;
import com.rental.entity.Notification;
import com.rental.mapper.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper extends BaseMapper<Notification, NotificationResponseDto> {
    // No methods needed - inherits from BaseMapper
}