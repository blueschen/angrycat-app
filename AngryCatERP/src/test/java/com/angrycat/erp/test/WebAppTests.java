package com.angrycat.erp.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.initialize.config.WebConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={RootConfig.class, WebConfig.class})
@WebAppConfiguration
public class WebAppTests {
	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;
	
	@Before
	public void setUp(){
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}
	
	@Test
	public void listSalesDetail()throws Throwable{
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/salesdetail2").accept(MediaType.ALL);
		ResultActions resultActions = mockMvc.perform(requestBuilder);
		
		ResultMatcher statusOk = MockMvcResultMatchers.status().isOk();
		resultActions.andExpect(statusOk);
		
		ResultMatcher redirectedUrl = MockMvcResultMatchers.view().name("list");
		resultActions.andExpect(redirectedUrl);
	}
}
