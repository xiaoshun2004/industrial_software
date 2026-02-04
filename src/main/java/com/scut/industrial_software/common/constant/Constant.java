package com.scut.industrial_software.common.constant;

import java.util.List;
import java.util.Map;

public class Constant {
    /**
     * 要素数据库类型
     */
    public static final List<String> dbTypes = List.of(
            "simulationResult", "exampleLibrary", "materialConstitutive",
            "connectionPerformance", "modelLibrary", "aircraftDummy"
    );
    /**
     * 获取模块分类
     */
    public static final Map<String, String> moduleCategories = Map.of(
            "pre", "前处理",
            "solver", "求解器",
            "post", "后处理"
    );
    /**
     * 获取一个模块下的模块列表
     */
    public static final Map<String,Map<String,String>> moduleLibrary = Map.of(
            "pre", Map.of(
                    "pre-impact", "冲击前处理",
                    "pre-struct", "结构前处理",
                    "pre-multibody", "多体前处理"
            ),
            "solver", Map.of(
                    "solver-impact-cpu", "冲击求解器CPU版",
                    "solver-impact-gpu", "冲击求解器GPU版",
                    "solver-struct", "结构求解器",
                    "solver-multibody", "多体求解器"
            ),
            "post", Map.of(
                    "post-impact", "冲击后处理",
                    "post-struct", "结构后处理",
                    "post-multibody", "多体后处理"
            )
    );


}
