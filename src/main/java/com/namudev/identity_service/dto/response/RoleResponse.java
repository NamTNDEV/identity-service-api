package com.namudev.identity_service.dto.response;

import com.namudev.identity_service.dto.request.PermissionRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class RoleResponse {
    String name;
    String description;
    Set<PermissionRequest> permissions;
}
