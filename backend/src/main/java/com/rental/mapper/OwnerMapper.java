package com.rental.mapper;

import com.rental.dto.OwnerDetailsDto;
import com.rental.dto.OwnerProfileDto;
import com.rental.dto.OwnerSummaryDto;
import com.rental.dto.RegisterResponseDto;
import com.rental.dto.LoginResponseDto;
import com.rental.entity.Owner;
import com.rental.mapper.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OwnerMapper extends BaseMapper<Owner, OwnerSummaryDto> {

    // ================================================================
    // BASE MAPPINGS (Entity ↔ Summary DTO)
    // ================================================================

    @Override
    @Mapping(target = "role", expression = "java(owner.getRole())")  // ✅ role is String
    @Mapping(target = "isActive", source = "isActive")
    OwnerSummaryDto toDto(Owner owner);

    @Override
    @Mapping(target = "role", expression = "java(dto.getRole())")  // ✅ role is String
    Owner toEntity(OwnerSummaryDto dto);

    // ================================================================
    // PROFILE MAPPING
    // ================================================================

    @Mapping(target = "role", expression = "java(owner.getRole())")  // ✅ role is String
    OwnerProfileDto toProfileDto(Owner owner);

    // ================================================================
    // DETAILS MAPPING (with additional fields)
    // ================================================================

    @Mapping(target = "listingCount", expression = "java(owner.getProperties() != null ? owner.getProperties().size() : 0)")
    @Mapping(target = "role", expression = "java(owner.getRole())")  // ✅ role is String
    OwnerDetailsDto toDetailsDto(Owner owner);

    // ================================================================
    // AUTHENTICATION RESPONSE MAPPINGS
    // ✅ No role mapping needed - role is already in Owner object
    // ================================================================

    @Mapping(target = "token", source = "token")
    @Mapping(target = "refreshToken", source = "refreshToken")
    RegisterResponseDto toRegisterResponseDto(Owner owner, String token, String refreshToken);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "refreshToken", source = "refreshToken")
    LoginResponseDto toLoginResponseDto(Owner owner, String token, String refreshToken);
}