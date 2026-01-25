package com.scut.industrial_software.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "component")
@Setter
@Getter
public class ComponentStoreProperties {

    /**
     * 组件安装包存储路径映射，key 对应 application.yaml 中 component.store 下的键
     */
    private Map<String, String> store = new HashMap<>();

    /**
     * 便捷获取具体安装路径
     */
    public String getPath(String key) {
        return store.get(key);
    }
}
