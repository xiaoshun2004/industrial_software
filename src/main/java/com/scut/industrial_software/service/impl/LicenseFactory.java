package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.service.ILicenseStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * License策略工厂类，用于根据不同的工具类型获取对应的License策略实现
 */
@Component
public class LicenseFactory {

    private final Map<String, ILicenseStrategy> strategies = new HashMap<>();

    // Spring 会自动将所有 LicenseStrategy 的实现类注入到这个构造函数
    public LicenseFactory(List<ILicenseStrategy> strategyList) {
        for (ILicenseStrategy strategy : strategyList) {
            strategies.put(strategy.getLicenseCreateType(), strategy);
        }
    }

    public ILicenseStrategy getStrategy(String toolType) {
        ILicenseStrategy strategy = strategies.get(toolType);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到对应的工具处理器: " + toolType);
        }
        return strategy;
    }
}
