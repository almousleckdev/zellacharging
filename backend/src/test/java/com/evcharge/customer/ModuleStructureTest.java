package com.evcharge.customer;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModuleStructureTest {

    static final ApplicationModules modules = ApplicationModules.of(NdzelaApplication.class);

    @Test
    void modulesRespectBoundaries() {
        modules.verify();
    }
}
