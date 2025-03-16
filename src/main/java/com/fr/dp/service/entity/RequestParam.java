package com.fr.dp.service.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author szx
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestParam {
    String name;
    String type;
    String pose;
    // 当为body参数，且请求体格式为json时，需要依赖jsonPath获取对应值
    String jsonPath;

    boolean required;

    Object defaultValue;

    // 重写equals和hashcode
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequestParam that = (RequestParam) o;

        if (!Objects.equals(name, that.name)) {
            return false;
        }
        if (!Objects.equals(type, that.type)) {
            return false;
        }
        return Objects.equals(pose, that.pose);
    }
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (pose != null ? pose.hashCode() : 0);
        return result;
    }
}
