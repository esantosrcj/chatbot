package com.santos.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santos.model.Shortcut;
import com.santos.service.ShortcutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.santos.util.Constant.HTTP_BAD_REQUEST;
import static com.santos.util.Constant.HTTP_OK;
import static com.santos.util.Constant.BOT_ID;

@Controller    // This means that this class is a Controller
@RequestMapping(path = "/bot") // This means URL's start with /bot (after Application path)
public class ChatController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

	@Autowired
	// This means to get the bean called shortcutService
	private ShortcutService shortcutService;

	@GetMapping(path = "/") // Shortcut for @RequestMapping(method=GET)
	public ResponseEntity<String> home() {
		// @ResponseBody means the returned String is the response, not a view name
		// @RequestParam means it is a parameter from the GET or POST request
		return new ResponseEntity<>("Home", HttpStatus.OK);
	}

	@PostMapping(path = "/add") // Shortcut for @RequestMapping(method=POST)
	@ResponseBody
	public ResponseEntity<String> addNewShortcut(@RequestBody Shortcut shortcut) {
		// @ResponseBody means the returned String is the response, not a view name
		// @RequestBody means it is an object of type User
		try {
			LocalDateTime now = LocalDateTime.now();
			shortcut.setCreated(now);
			shortcutService.addShortcut(shortcut);

			return new ResponseEntity<>(HTTP_OK, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
			return new ResponseEntity<>(HTTP_BAD_REQUEST, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/all")
	@ResponseBody
	public List<Shortcut> getAllShortcuts() {
		// This returns a JSON or XML with the users
		return shortcutService.findAll();
	}

	@PostMapping(path = "/callback") // Shortcut for @RequestMapping(method=POST)
	@ResponseBody
	public ResponseEntity<String> botCallback(@RequestBody String payload) {
		// @ResponseBody means the returned String is the response, not a view name
		// @RequestBody means it is an object of type User
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode node = mapper.readTree(payload);
			final String phrase = shortcutService.evaluateChatText(node);
			if (StringUtils.isEmpty(phrase)) {
				// An empty phrase means that normal conversation is happening
				return new ResponseEntity<>(HTTP_OK, HttpStatus.OK);
			}
			final String botId = System.getenv().get(BOT_ID);

			return shortcutService.postToChat(botId, phrase);
		} catch (Exception e) {
			LOGGER.error("An error occurred when calling [/callback] endpoint:", e);
			return new ResponseEntity<>(HTTP_BAD_REQUEST, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/joke") // Shortcut for @RequestMapping(method=GET)
	@ResponseBody
	public ResponseEntity<String> getJoke() {
		final String respString = shortcutService.getJoke("", false);
		final String joke = shortcutService.getJokeFromResponse(respString, false);
		if (StringUtils.isEmpty(joke)) {
			return new ResponseEntity<>("Unable to retrieve joke at this time", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(joke, HttpStatus.OK);
	}

	@GetMapping(path = "/joke/{term}") // Shortcut for @RequestMapping(method=GET)
	@ResponseBody
	public ResponseEntity<String> getJoke(@PathVariable String term) {
		final String respString = shortcutService.getJoke(term, true);
		final String joke = shortcutService.getJokeFromResponse(respString, true);
		if (StringUtils.isEmpty(joke)) {
			return new ResponseEntity<>("Unable to retrieve joke at this time", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(joke, HttpStatus.OK);
	}
}
