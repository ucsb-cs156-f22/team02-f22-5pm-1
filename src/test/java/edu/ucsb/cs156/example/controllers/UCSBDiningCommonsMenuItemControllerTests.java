package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenu;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuRepository;

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

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDiningCommonsMenuRepository ucsbDiningCommonsMenuRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdates/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenu/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenu/all"))
                                .andExpect(status().is(200)); // logged
        }

        
        // Authorization tests for /api/ucsbdiningcommonsmenu/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenu/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenu/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                

                UCSBDiningCommonsMenu ucsbDiningCommonsMenu = UCSBDiningCommonsMenu.builder()
                                .name("Chicken Caeser Salad")
                                .diningCommonsCode("ortega")
                                .station("Entrees")
                                .build();

                when(ucsbDiningCommonsMenuRepository.findById(eq(7L))).thenReturn(Optional.of(ucsbDiningCommonsMenu));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenu?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenu);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbDiningCommonsMenuRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenu?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenu with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdates() throws Exception {


                UCSBDiningCommonsMenu ucsbDiningCommonsMenu1 = UCSBDiningCommonsMenu.builder()
                                .name("Chicken Caesar Salad")
                                .diningCommonsCode("ortega")
                                .station("Entrees")
                                .build();


                UCSBDiningCommonsMenu ucsbDiningCommonsMenu2 = UCSBDiningCommonsMenu.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                ArrayList<UCSBDiningCommonsMenu> expectedDiningCommonsMenu = new ArrayList<>();
                expectedDiningCommonsMenu.addAll(Arrays.asList(ucsbDiningCommonsMenu1, ucsbDiningCommonsMenu2));

                when(ucsbDiningCommonsMenuRepository.findAll()).thenReturn(expectedDiningCommonsMenu);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenu/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDiningCommonsMenu);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        
}
