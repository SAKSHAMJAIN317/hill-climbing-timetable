package com.smartsched;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsched.dto.ScheduleResultDto;
import com.smartsched.dto.TimetableGenerationRequest;
import com.smartsched.repository.ClassroomRepository;
import com.smartsched.repository.ScheduleRepository;
import com.smartsched.repository.TimeSlotRepository;
import com.smartsched.service.DemoDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SmartSchedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DemoDataService demoDataService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        demoDataService.resetAndLoadDemoData("NORMAL");
    }

    @Test
    @DisplayName("GET /api/dashboard/summary should return live university metrics")
    void testDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFaculty", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.totalSubjects", greaterThanOrEqualTo(12)))
                .andExpect(jsonPath("$.totalBatches", greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$.totalRooms", greaterThanOrEqualTo(6)));
    }

    @Test
    @DisplayName("POST /api/schedules/generate with Hill Climbing should produce a VALID schedule")
    void testGenerateScheduleHillClimbing() throws Exception {
        TimetableGenerationRequest request = new TimetableGenerationRequest();
        request.setAlgorithm("HILL_CLIMBING");
        request.setName("Test Hill Climbing Timetable");
        request.setMaxIterations(2000);
        request.setRandomRestarts(5);

        mockMvc.perform(post("/api/schedules/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithmName", is("Hill Climbing")))
                .andExpect(jsonPath("$.status", is("VALID")))
                .andExpect(jsonPath("$.hardViolations", is(0)))
                .andExpect(jsonPath("$.assignments", not(empty())));
    }

    @Test
    @DisplayName("POST /api/schedules/generate without payload should use Hill Climbing by default")
    void testGenerateScheduleDefaultPayload() throws Exception {
        mockMvc.perform(post("/api/schedules/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithmName", is("Hill Climbing")))
                .andExpect(jsonPath("$.assignments", not(empty())));
    }

    @Test
    @DisplayName("GET /api/schedules/{id}/export.csv should return CSV file content")
    void testExportCsv() throws Exception {
        TimetableGenerationRequest request = new TimetableGenerationRequest();
        request.setName("Exportable Timetable");

        String responseContent = mockMvc.perform(post("/api/schedules/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ScheduleResultDto resDto = objectMapper.readValue(responseContent, ScheduleResultDto.class);
        assertNotNull(resDto.getScheduleId());

        mockMvc.perform(get("/api/schedules/" + resDto.getScheduleId() + "/export.csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(containsString("Day,Period,Start Time,End Time,Subject Code")));
    }

    @Test
    @DisplayName("POST /api/exams/generate should create examination schedule and export CSV")
    void testExamGenerationAndExport() throws Exception {
        mockMvc.perform(post("/api/exams/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(get("/api/exams/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(get("/api/exams/export.csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(containsString("Exam Date,Day,Start Time,End Time,Subject Code")));
    }

    @Test
    @DisplayName("POST /api/demo/load with IMPOSSIBLE scenario should configure infeasible problem")
    void testImpossibleScenarioThroughRest() throws Exception {
        mockMvc.perform(post("/api/demo/load?scenario=IMPOSSIBLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenario", is("IMPOSSIBLE")));

        TimetableGenerationRequest request = new TimetableGenerationRequest();
        request.setName("Impossible Timetable");

        mockMvc.perform(post("/api/schedules/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INFEASIBLE")));
    }
}
