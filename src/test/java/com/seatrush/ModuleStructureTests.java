package com.seatrush;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModuleStructureTests {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(SeatRushApplication.class).verify();
    }
}
