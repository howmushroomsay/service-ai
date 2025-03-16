package com.fr.dp.service;

import com.fr.dp.service.dto.ApiInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TesController {

    @Autowired
    private ApiRegistry apiRegistry;

    @PostMapping("/register")
    public String registerMethod(@RequestBody ApiInfoDTO apiInfoDTO) {
        apiRegistry.register(apiInfoDTO.getPath(), apiInfoDTO);
        log.info("registerMethod: {}", apiInfoDTO);
        return "注册成功";
    }

    @PostMapping("/unregister")
    public String unregisterMethod(@RequestBody ApiInfoDTO apiInfoDTO) {
        apiRegistry.unregister(apiInfoDTO.getPath());
        log.info("unregisterMethod: {}", apiInfoDTO);
        return "注销成功";
    }
}
