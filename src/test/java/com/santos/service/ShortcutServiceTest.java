package com.santos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santos.model.Shortcut;
import com.santos.repository.ShortcutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class ShortcutServiceTest {

	@Mock
	private ShortcutRepository shortcutRepository;

	private ShortcutService shortcutService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		shortcutService = new ShortcutService(shortcutRepository);
	}

	@DisplayName("Get all shortcuts")
	@Test
	void getAllShortcutsInTheRepository() {
		List<Shortcut> shortcuts = new ArrayList<>();
		Shortcut shortcut1 = new Shortcut();
		shortcuts.add(shortcut1);
		Shortcut shortcut2 = new Shortcut();
		shortcuts.add(shortcut2);

		when(shortcutRepository.findAll()).thenReturn(shortcuts);

		List<Shortcut> returnedShortcuts = shortcutService.findAll();

		assertEquals(returnedShortcuts.size(), 2);
	}

	@DisplayName("Add new shortcut")
	@Test
	void addNewShortcutToTheRepository() {
		Shortcut shortcut1 = new Shortcut();
		Shortcut shortcut2 = new Shortcut();
		when(shortcutRepository.save(shortcut1)).thenReturn(shortcut2);

		shortcutService.addShortcut(shortcut1);
	}

	@DisplayName("Delete a shortcut")
	@Test
	void deleteShortcutFromTheRepository() {
		Shortcut shortcut = new Shortcut();
		doNothing().when(shortcutRepository).delete(any(Shortcut.class));

		shortcutService.deleteShortcut(shortcut);

		verify(shortcutRepository, times(1)).delete(shortcut);
	}

	@DisplayName("Successfully find a shortcut")
	@Test
	void getShortcutWithTheCommandForTheShortcutSuccess() {
		List<Shortcut> shortcuts = new ArrayList<>();
		Shortcut shortcut1 = new Shortcut();
		shortcut1.setId(10);
		shortcut1.setCommand("BYOB");
		shortcuts.add(shortcut1);
		Shortcut shortcut2 = new Shortcut();
		shortcuts.add(shortcut2);

		when(shortcutRepository.findAll()).thenReturn(shortcuts);

		Optional<Shortcut> shortcutOpt = shortcutService.getShortcut("byob");

		assertTrue(shortcutOpt.isPresent());
		assertEquals(shortcutOpt.get().getId(), new Integer(10));
	}

	@DisplayName("Fail to find a shortcut")
	@Test
	void getShortcutWithTheCommandForTheShortcutFailure() {
		List<Shortcut> shortcuts = new ArrayList<>();
		Shortcut shortcut1 = new Shortcut();
		shortcut1.setId(10);
		shortcut1.setCommand("BYOB");
		shortcuts.add(shortcut1);
		Shortcut shortcut2 = new Shortcut();
		shortcuts.add(shortcut2);

		when(shortcutRepository.findAll()).thenReturn(shortcuts);

		Optional<Shortcut> shortcutOpt = shortcutService.getShortcut("yolo");

		assertFalse(shortcutOpt.isPresent());
	}

	@DisplayName("Normal chat conversation")
	@Test
	void evaluateTextThatIsNormalChatConversations() throws IOException {
		String jsonString = "{\"text\":\"Normal chatting\",\"user_id\":\"1234\"}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(jsonString);

		String response = shortcutService.evaluateChatText(node);

		assertTrue(StringUtils.isEmpty(response));
	}

	@DisplayName("Text to add new shortcut")
	@Test
	void evaluateTextForAddingANewShortcut() throws IOException {
		String jsonString = "{\"text\":\"!byob: bring your own beer\",\"user_id\":\"1234\",\"name\":\"John Doe\"}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(jsonString);

		when(shortcutRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

		String response = shortcutService.evaluateChatText(node);

		assertTrue(StringUtils.isEmpty(response));
	}

	@DisplayName("Text to request shortcut")
	@Test
	void evaluateTextForRequestingAShortcutWithAValidCommand() throws IOException {
		Shortcut shortcut = new Shortcut();
		shortcut.setId(10);
		shortcut.setCommand("YOLO");
		shortcut.setPhrase("You only live once");
		shortcut.setUserId("1111");
		shortcut.setUsername("Anon");
		List<Shortcut> shortcuts = Collections.singletonList(shortcut);

		String jsonString = "{\"text\":\"!yolo\",\"user_id\":\"1111\"}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(jsonString);

		when(shortcutRepository.findAll()).thenReturn(shortcuts);

		String response = shortcutService.evaluateChatText(node);

		assertEquals(response, "You only live once");
	}

	@DisplayName("Text to add new shortcut")
	@Test
	void evaluateTextForRequestingAShortcutWithAnInvalidCommand() throws IOException {
		String jsonString = "{\"text\":\"!leggo\",\"user_id\":\"1234\",\"name\":\"John Doe\"}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(jsonString);

		when(shortcutRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

		String response = shortcutService.evaluateChatText(node);

		assertEquals(response, "Oof. I don't know that one");
	}

	@DisplayName("Text that deletes shortcut")
	@Test
	void evaluateTextForKeywordThatDeletesAShortcutFromTheChat() throws IOException {
		Shortcut shortcut = new Shortcut();
		shortcut.setId(10);
		shortcut.setCommand("leggo");
		shortcut.setPhrase("let's go");
		shortcut.setUserId("1111");
		shortcut.setUsername("Anon");
		List<Shortcut> shortcuts = Collections.singletonList(shortcut);

		String jsonString = "{\"text\":\"!del leggo\",\"user_id\":\"1111\"}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(jsonString);

		when(shortcutRepository.findAll()).thenReturn(shortcuts);

		String response = shortcutService.evaluateChatText(node);

		assertEquals(response, "Poof. Gone.");
	}

	@DisplayName("GET API joke response")
	@Test
	void getJokeResponseFromGETAPICall() throws Exception {
		String responseString = shortcutService.getJoke("", false);

		assertNotNull(responseString);
	}

	@DisplayName("Parse response for random dad joke  ")
	@Test
	void getRandomJokeFromTheResponseBodyFromTheAPICall() {
		String jsonString = "{\"id\":\"abc123\",\"joke\":\"Hilarious joke\",\"status\":200}";
		String joke = shortcutService.getJokeFromResponse(jsonString, false);

		assertThat(joke, equalTo("Hilarious joke"));
	}

	@DisplayName("Parse response for specific dad joke  ")
	@Test
	void getSpecificJokeFromTheResponseBodyFromTheAPICall() {
		String jsonString = "{\"results\":[{\"id\":\"abc123\",\"joke\":\"First joke\"},{\"id\":\"cde456\",\"joke\":\"Second joke\"}]}";
		String joke = shortcutService.getJokeFromResponse(jsonString, true);

		assertNotNull(joke);
	}

	@Test
	@Disabled("Not implemented yet")
	void postToChat() {
	}
}