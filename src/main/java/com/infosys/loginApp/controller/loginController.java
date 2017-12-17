package com.infosys.loginApp.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.request.Request;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.loginApp.service.LoginService;



@RestController
public class loginController {

	@PostMapping("/login")
	public Response createUser(@RequestBody String requestBody)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			Request request = objectMapper.readValue(requestBody, Request.class);
			Map<String,Object> map = request.getRequest();
			LoginService loginService = new LoginService();
			loginService.createUser(request);
			/*Set<String> keys = map.keySet();
			for(String key: keys)
			{
				System.out.println("hi");
				System.out.println(key);
			}*/
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		Response response = new Response();
		return response;
	}

}