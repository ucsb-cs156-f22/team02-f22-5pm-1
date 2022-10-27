package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/recommendations?id=7"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    // Controller test for get

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
}
