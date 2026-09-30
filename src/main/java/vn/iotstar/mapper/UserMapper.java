package vn.iotstar.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(
            target = "id",
            source = "id"
    )
    @Mapping(
            target = "username",
            source = "username"
    )
    @Mapping(
            target = "email",
            source = "email"
    )
    @Mapping(
            target = "fullName",
            source = "fullName"
    )
    @Mapping(
            target = "images",
            source = "images"
    )
    @Mapping(
            target = "roleId",
            source = "role.id"
    )
    @Mapping(
            target = "roleName",
            source = "role.name"
    )
    @Mapping(
            target = "enabled",
            source = "enabled"
    )
    @Mapping(
            target = "productCount",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            source = "createdAt"
    )
    UserDTO toDTO(User entity);

    @Mapping(
            target = "id",
            source = "id"
    )
    @Mapping(
            target = "username",
            source = "username"
    )
    @Mapping(
            target = "email",
            source = "email"
    )
    @Mapping(
            target = "fullName",
            source = "fullName"
    )
    @Mapping(
            target = "images",
            source = "images"
    )
    @Mapping(
            target = "enabled",
            source = "enabled"
    )
    @Mapping(
            target = "password",
            ignore = true
    )
    @Mapping(
            target = "role",
            ignore = true
    )
    @Mapping(
            target = "products",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            source = "createdAt"
    )
    User toEntity(UserDTO dto);
}