package com.santos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping(path = "/")
public class HomeController {

	@GetMapping(path = "/") // Shortcut for @RequestMapping(method=GET)
	public ResponseEntity<String> home() {
		// @ResponseBody means the returned String is the response, not a view name
		// @RequestParam means it is a parameter from the GET or POST request
		return new ResponseEntity<>("Welcome to the ChatBot App", HttpStatus.OK);
	}
}
