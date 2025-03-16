package com.fr.dp.service.dto;

import com.fr.dp.service.entity.RequestParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfoDTO {

    private Set<RequestParam> params;

    private String method;

    private String contentType;

    private String path;

    private String secretKey;


}
