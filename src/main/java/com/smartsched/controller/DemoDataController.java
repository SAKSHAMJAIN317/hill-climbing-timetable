package com.smartsched.controller;

import com.smartsched.service.DemoDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoDataController {

    private final DemoDataService demoDataService;

    public DemoDataController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @PostMapping("/load")
    public ResponseEntity<Map<String, String>> loadDemoData(
            @RequestParam(defaultValue = "NORMAL") String scenario) {
        demoDataService.resetAndLoadDemoData(scenario);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "scenario", scenario.toUpperCase(),
                "message", "Demo dataset for scenario '" + scenario.toUpperCase() + "' successfully initialized."
        ));
    }
}
