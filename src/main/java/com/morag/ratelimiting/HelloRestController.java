package com.morag.ratelimiting;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloRestController {

	@GetMapping("/hello")
	ResponseEntity hello() {		
		return new ResponseEntity("You made it mate\n", HttpStatus.OK);
	}


}
