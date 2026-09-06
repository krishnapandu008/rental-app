package com.rental.mapper;

import com.rental.dto.InquiryResponseDto;
import com.rental.entity.Inquiry;
import com.rental.mapper.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface InquiryMapper extends BaseMapper<Inquiry, InquiryResponseDto> {

    @Override
    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "propertyTitle", source = "property.title")
    @Mapping(target = "senderName", source = "sender.name")
    @Mapping(target = "senderEmail", source = "sender.email")
    InquiryResponseDto toDto(Inquiry inquiry);

    @Override
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "sender", ignore = true)
    Inquiry toEntity(InquiryResponseDto dto);
}