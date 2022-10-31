package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@WebMvcTest(controllers = RecommendationController.class)
@Import(TestConfig.class)
public class RecommendationControllerTests extends ControllerTestCase {
    @MockBean
    RecommendationRepository recommendationRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for get

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendations/all"))
                .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendations/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/recommendations?id=7"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    // Controller test for get

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_recommendations() throws Exception {

        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");
        LocalDateTime ldt3 = LocalDateTime.parse("2022-01-05T00:00:00");
        LocalDateTime ldt4 = LocalDateTime.parse("2022-01-06T00:00:00");


        Recommendation recommendation1 = Recommendation.builder()
                        .requesterEmail("requester1@mail.com")
                        .professorEmail("professor1@mail.com")
                        .explanation("test1")
                        .dateRequested(ldt1)
                        .dateNeeded(ldt2)
                        .done(true)
                        .build();

        Recommendation recommendation2 = Recommendation.builder()
                        .requesterEmail("requester2@mail.com")
                        .professorEmail("professor2@mail.com")
                        .explanation("test2")
                        .dateRequested(ldt3)
                        .dateNeeded(ldt4)
                        .done(false)
                        .build();

        ArrayList<Recommendation> expectedRecs = new ArrayList<>();
        expectedRecs.addAll(Arrays.asList(recommendation1, recommendation2));

        when(recommendationRepository.findAll()).thenReturn(expectedRecs);

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendations/all"))
                        .andExpect(status().isOk()).andReturn();

        // assert

        verify(recommendationRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedRecs);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

        // arrange

        when(recommendationRepository.findById(eq(7L))).thenReturn(Optional.empty());

        // act

        MvcResult response = mockMvc.perform(get("/api/recommendations?id=7"))
                        .andExpect(status().isNotFound()).andReturn();
        
        // assert

        verify(recommendationRepository, times(1)).findById(eq(7L));
        
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        // arrange
        LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

        Recommendation recommendation = Recommendation.builder()
                        .requesterEmail("requester@mail.com")
                        .professorEmail("professor@mail.com")
                        .explanation("test")
                        .dateRequested(ldt)
                        .dateNeeded(ldt)
                        .done(false)
                        .build();

        when(recommendationRepository.findById(eq(7L))).thenReturn(Optional.of(recommendation));

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendations?id=7"))
                        .andExpect(status().isOk()).andReturn();

        // assert

        verify(recommendationRepository, times(1)).findById(eq(7L));
        String expectedJson = mapper.writeValueAsString(recommendation);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Controller tests for POST

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendation() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");

        Recommendation recommendation = Recommendation.builder()
                        .requesterEmail("requester@mail.com")
                        .professorEmail("professor@mail.com")
                        .explanation("test")
                        .dateRequested(ldt1)
                        .dateNeeded(ldt2)
                        .done(true)
                        .build();

        when(recommendationRepository.save(eq(recommendation))).thenReturn(recommendation);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/recommendations/post?requesterEmail=requester@mail.com&professorEmail=professor@mail.com&explanation=test&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-01-04T00:00:00&done=true")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRepository, times(1)).save(recommendation);
        String expectedJson = mapper.writeValueAsString(recommendation);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Controller test for DELETE

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_rec() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");

        Recommendation recommendation = Recommendation.builder()
                    .requesterEmail("requester@mail.com")
                    .professorEmail("professor@mail.com")
                    .explanation("test")
                    .dateRequested(ldt1)
                    .dateNeeded(ldt2)
                    .done(false)
                    .build();

        when(recommendationRepository.findById(eq(15L))).thenReturn(Optional.of(recommendation));
        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/recommendations?id=15")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRepository, times(1)).findById(15L);
        verify(recommendationRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("Recommendation with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_recommendation_and_gets_right_error_message()
                throws Exception {
        // arrange

        when(recommendationRepository.findById(eq(15L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/recommendations?id=15")
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(recommendationRepository, times(1)).findById(15L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Recommendation with id 15 not found", json.get("message"));
    }

    // Controller tests for PUT endpoint

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_recommendation() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-01T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2023-01-02T00:00:00");
        LocalDateTime ldt3 = LocalDateTime.parse("2023-01-03T00:00:00");
        LocalDateTime ldt4 = LocalDateTime.parse("2023-01-04T00:00:00");

        Recommendation recommendationOrig = Recommendation.builder()
                        .requesterEmail("req@mail.com")
                        .professorEmail("prof@mail.com")
                        .explanation("original")
                        .dateRequested(ldt1)
                        .dateNeeded(ldt2)
                        .done(false)
                        .build();

        Recommendation recommendationEdited = Recommendation.builder()
                        .requesterEmail("dantik@mail.com")
                        .professorEmail("lusik@mail.com")
                        .explanation("edited ft my cats")
                        .dateRequested(ldt3)
                        .dateNeeded(ldt4)
                        .done(true)
                        .build();

        String requestBody = mapper.writeValueAsString(recommendationEdited);

        when(recommendationRepository.findById(eq(67L))).thenReturn(Optional.of(recommendationOrig));

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/recommendations?id=67")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRepository, times(1)).findById(67L);
        verify(recommendationRepository, times(1)).save(recommendationEdited); // should be saved with correct user
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_recommendation_that_does_not_exist() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");

        Recommendation recommendationEdited = Recommendation.builder()
                        .requesterEmail("dantik@mail.com")
                        .professorEmail("lusik@mail.com")
                        .explanation("edited ft my cats")
                        .dateRequested(ldt1)
                        .dateNeeded(ldt2)
                        .done(true)
                        .build();

        String requestBody = mapper.writeValueAsString(recommendationEdited);

        when(recommendationRepository.findById(eq(67L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/recommendations?id=67")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(recommendationRepository, times(1)).findById(67L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Recommendation with id 67 not found", json.get("message"));
    }
}