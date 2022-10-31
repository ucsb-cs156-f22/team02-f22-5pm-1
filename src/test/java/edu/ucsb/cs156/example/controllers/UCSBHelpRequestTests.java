package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBHelpRequest;
import edu.ucsb.cs156.example.repositories.UCSBHelpRequestRepository;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBHelpRequestController.class)
@Import(TestConfig.class)
public class UCSBHelpRequestTests extends ControllerTestCase {

        @MockBean
        UCSBHelpRequestRepository ucsbHelpRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdates/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbhelprequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbhelprequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbhelprequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbdates/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbhelprequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbhelprequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBHelpRequest ucsbHelpRequest = UCSBHelpRequest.builder()
                                .requesterEmail("batman@wayne.com")
                                .teamId("2022")
                                .tableOrBreakoutRoom("BreakoutRoom")
                                .requestTime(ldt)
                                .explanation("I'm batman!")
                                .solved(false)
                                .build();

                when(ucsbHelpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(ucsbHelpRequest));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbhelprequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbHelpRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(ucsbHelpRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbHelpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbhelprequest?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbHelpRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBHelpRequest with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdates() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBHelpRequest ucsbHelpRequest1 = UCSBHelpRequest.builder()
                                .requesterEmail("batman@wayne.com")
                                .teamId("2022")
                                .tableOrBreakoutRoom("BreakoutRoom")
                                .requestTime(ldt1)
                                .explanation("I'm batman!")
                                .solved(false)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                UCSBHelpRequest ucsbHelpRequest2 = UCSBHelpRequest.builder()
                                .requesterEmail("batman@wayne.com")
                                .teamId("2023")
                                .tableOrBreakoutRoom("BreakoutRoom")
                                .requestTime(ldt2)
                                .explanation("I'm batman!")
                                .solved(false)
                                .build();

                ArrayList<UCSBHelpRequest> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(ucsbHelpRequest1, ucsbHelpRequest2));

                when(ucsbHelpRequestRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbhelprequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbHelpRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbdate() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBHelpRequest ucsbHelpRequest1 = UCSBHelpRequest.builder()
                .explanation("I'm batman!")
                .requestTime(ldt1)
                .requesterEmail("batman@wayne.com")
                .solved(false)
                .tableOrBreakoutRoom("table")
                .teamId("2023")
                .build();


                when(ucsbHelpRequestRepository.save(eq(ucsbHelpRequest1))).thenReturn(ucsbHelpRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbhelprequest/post?explanation=I'm batman!&requestTime=2022-01-03T00:00:00&requesterEmail=batman@wayne.com&solved=false&tableOrBreakoutRoom=table&teamId=2023")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).save(ucsbHelpRequest1);
                String expectedJson = mapper.writeValueAsString(ucsbHelpRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbdate1() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBHelpRequest ucsbHelpRequest1 = UCSBHelpRequest.builder()
                .explanation("I'm batman!")
                .requestTime(ldt1)
                .requesterEmail("batman@wayne.com")
                .solved(true)
                .tableOrBreakoutRoom("table")
                .teamId("2023")
                .build();


                when(ucsbHelpRequestRepository.save(eq(ucsbHelpRequest1))).thenReturn(ucsbHelpRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbhelprequest/post?explanation=I'm batman!&requestTime=2022-01-03T00:00:00&requesterEmail=batman@wayne.com&solved=true&tableOrBreakoutRoom=table&teamId=2023")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).save(ucsbHelpRequest1);
                String expectedJson = mapper.writeValueAsString(ucsbHelpRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBHelpRequest ucsbHelpRequest1 = UCSBHelpRequest.builder()
                .requesterEmail("batman@wayne.com")
                .teamId("2023")
                .tableOrBreakoutRoom("BreakoutRoom")
                .requestTime(ldt1)
                .explanation("I'm batman!")
                .solved(true)
                .build();

                when(ucsbHelpRequestRepository.findById(eq(15L))).thenReturn(Optional.of(ucsbHelpRequest1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbhelprequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).findById(15L);
                verify(ucsbHelpRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBHelpRequest with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbHelpRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbhelprequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBHelpRequest with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdate() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                UCSBHelpRequest ucsbHelpRequestOrig = UCSBHelpRequest.builder()
                    .requesterEmail("batman@wayne.com")
                    .teamId("2023")
                    .tableOrBreakoutRoom("BreakoutRoom")
                    .requestTime(ldt1)
                    .explanation("I'm batman!")
                    .solved(false)
                    .build();
                    UCSBHelpRequest ucsbHelpRequestEdited = UCSBHelpRequest.builder()
                .requesterEmail("batman@wayne111.com")
                .teamId("2023111")
                .tableOrBreakoutRoom("table")
                .requestTime(ldt2)
                .explanation("I'm batman111!")
                .solved(true)
                .build();

                String requestBody = mapper.writeValueAsString(ucsbHelpRequestEdited);

                when(ucsbHelpRequestRepository.findById(eq(67L))).thenReturn(Optional.of(ucsbHelpRequestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbhelprequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).findById(67L);
                verify(ucsbHelpRequestRepository, times(1)).save(ucsbHelpRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                UCSBHelpRequest ucsbEditedHelpRequest = UCSBHelpRequest.builder()
                .requesterEmail("batman@wayne111.com")
                .teamId("2023111")
                .tableOrBreakoutRoom("BreakoutRoom")
                .requestTime(ldt1)
                .explanation("I'm batman111!")
                .solved(false)
                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedHelpRequest);

                when(ucsbHelpRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbhelprequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBHelpRequest with id 67 not found", json.get("message"));

        }
}
