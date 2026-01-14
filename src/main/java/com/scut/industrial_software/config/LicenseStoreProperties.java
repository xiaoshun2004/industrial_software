package com.scut.industrial_software.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "license.store")
@Setter
@Getter
public class LicenseStoreProperties {

    private String impactTreatmentStorePath;

    private String impactSolverStorePath;

    private String multipleTreatmentStorePath;

    private String structureStorePath;
}
