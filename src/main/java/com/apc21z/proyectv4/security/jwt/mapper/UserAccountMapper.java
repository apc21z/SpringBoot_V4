package com.apc21z.proyectv4.security.jwt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.apc21z.proyectv4.security.jwt.dto.RegisterRequest;
import com.apc21z.proyectv4.security.jwt.dto.UserAccountDTO;
import com.apc21z.proyectv4.security.jwt.model.UserAccount;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "roles", ignore = true)
    UserAccount toEntity(RegisterRequest request, String encodedPassword);

    UserAccountDTO toDto(UserAccount userAccount);
}