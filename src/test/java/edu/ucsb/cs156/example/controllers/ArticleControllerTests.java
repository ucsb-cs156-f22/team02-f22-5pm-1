package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticleRepository;

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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ArticleController.class)
@Import(TestConfig.class)
public class ArticleControllerTests extends ControllerTestCase {

        @MockBean
        ArticleRepository articleRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/Article/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/Article/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/Article/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/Article?title=article"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }


        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_articles() throws Exception {

                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Article article1 = Article.builder()
                                .title("ArticleOne")
                                .url("articleone")
                                .explanation("articleone explanation")
                                .email("liamjet@gmail.com")
                                .dateAdded(ldt)
                                .build();

                Article article2 = Article.builder()
                                .title("ArticleTwo")
                                .url("articletwo")
                                .explanation("articletwo explanation")
                                .email("liamjet@gmail.com")
                                .dateAdded(ldt)
                                .build();

                ArrayList<Article> expectedArticle = new ArrayList<>();
                expectedArticle.addAll(Arrays.asList(article1, article2));

                when(articleRepository.findAll()).thenReturn(expectedArticle);

                // act
                MvcResult response = mockMvc.perform(get("/api/Article/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(articleRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedArticle);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Authorization tests for /api/Article/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Article/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Article/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Article article = Article.builder()
                                .title("ArticleOne")
                                .url("articleone")
                                .explanation("articleone explanation")
                                .email("liamjet@gmail.com")
                                .dateAdded(ldt)
                                .build();

                when(articleRepository.findById(eq("ArticleOne"))).thenReturn(Optional.of(article));

                // act
                MvcResult response = mockMvc.perform(get("/api/Article?title=ArticleOne"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(articleRepository, times(1)).findById(eq("ArticleOne"));
                String expectedJson = mapper.writeValueAsString(article);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(articleRepository.findById(eq("ArticleDoesntExist"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/Article?title=ArticleDoesntExist"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(articleRepository, times(1)).findById(eq("ArticleDoesntExist"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Article with id ArticleDoesntExist not found", json.get("message"));
        }

        // logged in user can get all

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_article() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Article article3 = Article.builder()
                                .title("ArticleThree")
                                .url("articlethree")
                                .explanation("articlethreeexplanation")
                                .email("liamjet@gmail.com")
                                .dateAdded(ldt)
                                .build();

                when(articleRepository.save(eq(article3))).thenReturn(article3);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/Article/post?title=ArticleThree&url=articlethree&explanation=articlethreeexplanation&email=liamjet@gmail.com&dateAdded=2022-01-03T00:00:00")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articleRepository, times(1)).save(article3);
                String expectedJson = mapper.writeValueAsString(article3);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Article article4 = Article.builder()
                        .title("ArticleFour")
                        .url("articlefour")
                        .explanation("articlefour explanation")
                        .email("liamjet@gmail.com")
                        .dateAdded(ldt)
                        .build();

                when(articleRepository.findById(eq("ArticleFour"))).thenReturn(Optional.of(article4));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Article?title=ArticleFour")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articleRepository, times(1)).findById("ArticleFour");
                verify(articleRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Article with id ArticleFour deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_article_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(articleRepository.findById(eq("ArticleDoesntExist"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Article?title=ArticleDoesntExist")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(articleRepository, times(1)).findById("ArticleDoesntExist");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Article with id ArticleDoesntExist not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_article() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldtedited = LocalDateTime.parse("2022-01-03T00:00:01");


                Article article = Article.builder()
                                .title("ArticleOne")
                                .url("articleone")
                                .explanation("articleone explanation")
                                .email("liamjet@gmail.com")
                                .dateAdded(ldt)
                                .build();

                Article articleEdited = Article.builder()
                                .title("ArticleOneEdited")
                                .url("articleoneedited")
                                .explanation("articleoneedited explanation")
                                .email("liamjetedited@gmail.com")
                                .dateAdded(ldtedited)
                                .build();

                String requestBody = mapper.writeValueAsString(articleEdited);

                when(articleRepository.findById(eq("ArticleOne"))).thenReturn(Optional.of(article));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Article?title=ArticleOne")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articleRepository, times(1)).findById("ArticleOne");
                verify(articleRepository, times(1)).save(articleEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_article_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Article editedArticle = Article.builder()
                    .title("ArticleDoesntExist")
                    .url("articledoesntexist")
                    .explanation("articledoesntexist explanation")
                    .email("liamjet@gmail.com")
                    .dateAdded(ldt)
                    .build();

                String requestBody = mapper.writeValueAsString(editedArticle);

                when(articleRepository.findById(eq("ArticleDoesntExist"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Article?title=ArticleDoesntExist")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(articleRepository, times(1)).findById("ArticleDoesntExist");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Article with id ArticleDoesntExist not found", json.get("message"));
        }
}
