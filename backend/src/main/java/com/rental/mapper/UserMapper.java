package com.rental.mapper;

import com.rental.dto.UserResponseDto;
import com.rental.entity.Owner;
import com.rental.entity.User;
import com.rental.mapper.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper extends BaseMapper<User, UserResponseDto> {

    @Override
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    UserResponseDto toDto(User user);

    @Override
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "role", expression = "java(dto.getRole() != null ? com.rental.enums.UserRole.valueOf(dto.getRole()) : null)")
    User toEntity(UserResponseDto dto);

    @Mapping(target = "isActive", source = "isActive")
    // ✅ Fixed: convert enum to String with .name()
    @Mapping(target = "role", expression = "java(owner.getRole() != null ? owner.getRole().name() : null)")
    UserResponseDto toDtoFromOwner(Owner owner);
}