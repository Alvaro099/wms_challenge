package io.tenoro.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.tenoro.app.api.dto.*;
import io.tenoro.app.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureWebMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WmsIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @DisplayName("DataSeeder debe haber cargado las ubicaciones iniciales")
    void testSeededLocationsExist() throws Exception {
        mockMvc.perform(get("/locations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[?(@.code == 'PICK-01')].type", contains("PICKING")))
                .andExpect(jsonPath("$[?(@.code == 'RSV-01')].type", contains("RESERVE")));
    }

    @Test
    @DisplayName("DataSeeder debe haber cargado el stock inicial")
    void testSeededStockExists() throws Exception {
        mockMvc.perform(get("/stock").param("sku", "SKU-100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.locationCode == 'PICK-01')].quantity", contains(5)))
                .andExpect(jsonPath("$[?(@.locationCode == 'RSV-01')].quantity", contains(60)));
    }

    @Test
    @DisplayName("Debe crear una nueva ubicación vía POST /locations")
    void testCreateLocationEndpoint() throws Exception {
        CreateLocationRequest request = new CreateLocationRequest("PICK-99", LocationTypeDto.PICKING);

        mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("PICK-99")))
                .andExpect(jsonPath("$.type", is("PICKING")));
    }

    @Test
    @DisplayName("Debe mover stock vía POST /stock/move")
    void testMoveStockEndpoint() throws Exception {
        MoveStockRequest request = new MoveStockRequest("SKU-100", "RSV-01", "PICK-01", 20);

        mockMvc.perform(post("/stock/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/stock").param("sku", "SKU-100").param("location", "PICK-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity", is(25)));

        mockMvc.perform(get("/stock").param("sku", "SKU-100").param("location", "RSV-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity", is(40)));
    }

    @Test
    @DisplayName("Debe evaluar y generar tareas de reabasto para SKU-100 en PICK-01")
    void testGenerateReplenishmentTasksEndpoint() throws Exception {
        GenerateTaskRequest request = new GenerateTaskRequest("SKU-100", "PICK-01");

        MvcResult result = mockMvc.perform(post("/replenishment/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        TaskResponse[] tasks = objectMapper.readValue(result.getResponse().getContentAsString(), TaskResponse[].class);
        assertEquals("RSV-01", tasks[0].fromLocation());
        assertEquals(60, tasks[0].quantity());
        assertEquals(TaskStatusDto.OPEN, tasks[0].status());

        mockMvc.perform(post("/replenishment/tasks/" + tasks[0].id() + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        mockMvc.perform(get("/stock").param("sku", "SKU-100").param("location", "PICK-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity", is(65)));

        mockMvc.perform(post("/replenishment/tasks/" + tasks[1].id() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }
}
