package com.fr.dp.service.context;

import java.util.HashMap;
import java.util.Map;

public class BuilderContext {
    private Map<String, Object> params = new HashMap<>();

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void addParam(String key, Object value) {
        this.params.put(key, value);
    }

    public Object getParam(String key) {
        return this.params.get(key);
    }

}
