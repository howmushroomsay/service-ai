package com.fr.dp.service;

import com.fr.dp.service.dto.ApiInfoDTO;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRegistry {
    private final ConcurrentHashMap<String, ApiInfoDTO> keyMap = new ConcurrentHashMap<>();

    public void register(String key, ApiInfoDTO apiInfoDTO) {
        keyMap.put(key, apiInfoDTO);
    }

    public void unregister(String key) {
        keyMap.remove(key);
    }

    public ApiInfoDTO get(String key) {
        return keyMap.get(key);
    }
}
