package com.bikeparts.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebController - Unit Tests")
class WebControllerTest {

    @InjectMocks
    private WebController webController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(webController)
                .setViewResolvers(resolver)
                .build();
    }


    // =========================================================
    // GET /login
    // =========================================================

    @Nested
    @DisplayName("login() - GET /login")
    class Login {

        @Test
        @DisplayName("returns login view")
        void login_returnsLoginView() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"));
        }
    }


    // =========================================================
    // GET /error
    // =========================================================

    @Nested
    @DisplayName("error() - GET /error")
    class ErrorPage {

        @Test
        @DisplayName("returns error view")
        void error_returnsErrorView() throws Exception {
            mockMvc.perform(get("/error"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("error"));
        }
    }


    // =========================================================
    // GET /
    // =========================================================

    @Nested
    @DisplayName("home() - GET /")
    class Home {

        @Test
        @DisplayName("redirects to /bikes")
        void home_redirectsToBikes() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bikes"));
        }
    }
}
