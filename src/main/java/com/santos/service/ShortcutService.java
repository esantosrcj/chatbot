package com.santos.service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santos.model.DadJoke;
import com.santos.model.Shortcut;
import com.santos.repository.ShortcutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.santos.util.Constant.*;

@Service
public class ShortcutService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShortcutService.class);
	private static final int DICE = 2;

	private ShortcutRepository shortcutRepository;

	@Autowired
	public ShortcutService(ShortcutRepository shortcutRepository) {
		this.shortcutRepository = shortcutRepository;
	}

	public List<Shortcut> findAll() {
		LOGGER.debug("Find all the shortcuts in the repository");
		return (List<Shortcut>) shortcutRepository.findAll();
	}

	public void addShortcut(Shortcut shortcut) {
		LOGGER.debug("Add shortcut with command [" + shortcut.getCommand() + "]");
		shortcutRepository.save(shortcut);
	}

	public void deleteShortcut(Shortcut shortcut) {
		LOGGER.debug("Delete shortcut with command [" + shortcut.getCommand() + "]");
		shortcutRepository.delete(shortcut);
	}

	public Optional<Shortcut> getShortcut(String command) {
		LOGGER.debug("Add shortcut with command [" + command + "]");
		final List<Shortcut> shortcuts = (List<Shortcut>) shortcutRepository.findAll();

		return shortcuts.stream().filter(s -> command.equalsIgnoreCase(s.getCommand())).findFirst();
	}

	public String evaluateChatText(JsonNode node) {
		final String text = node.get("text").asText();
		final String userId = node.get("user_id").asText();
		// For a new command look for ! [command] :
		if (text.indexOf('!') == 0 && text.indexOf(':') != -1) {
			// Colon will end command and text after that will be the phrase
			int colonIndex = text.indexOf(':');
			final String command = text.substring(1, colonIndex).trim();
			final String[] commandSplit = command.split(SPACE_REGEX);
			if (commandSplit.length > 1) {
				LOGGER.warn("Invalid format for requesting a shortcut");
				// No spaces allowed in commands
				return "Sorry bro. Invalid format";
			} else {
				final String phrase = text.substring(colonIndex + 1).trim();
				if (phrase.length() == 0) {
					// Cannot store empty phrase
					return "Umm...what do you want me to hype up?";
				} else if (phrase.length() > 255) {
					LOGGER.warn("Phrase is too long to store");
					// Cannot store empty phrase
					return "Whoa bro! Too many characters";
				} else {
					final boolean isKeyword = KEYWORDS.stream().anyMatch(k -> k.equalsIgnoreCase(command));
					if (isKeyword) {
						// Do not store keyword
						return "Smh. Cannot override a keyword";
					} else {
						final Optional<Shortcut> shortcutOpt = getShortcut(command);
						if (!shortcutOpt.isPresent()) {
							// Store command and phrase
							final String username = node.get("name").asText();
							final LocalDateTime now = LocalDateTime.now();
							final Shortcut shortcut = new Shortcut();
							shortcut.setCommand(command);
							shortcut.setPhrase(phrase);
							shortcut.setUsername(username);
							shortcut.setUserId(userId);
							shortcut.setCreated(now);
							addShortcut(shortcut);

							// Return empty string if nothing needs to be done
							return "";
						} else {
							LOGGER.warn("Unable store shortcut because command [" + command + "] already exists");
							return "Oof. Someone stole yo command. Try something else";
						}
					}
				}
			}
		} else if (text.indexOf('!') == 0) {
			final String[] textSplit = text.split(SPACE_REGEX);
			final String command = textSplit[0].substring(1).trim();
			final boolean isKeyword = KEYWORDS.stream().anyMatch(k -> k.equalsIgnoreCase(command));
			if (isKeyword) {
				return keywordOutput(command, textSplit, userId);
			} else {
				final Optional<Shortcut> shortcutOpt = getShortcut(command);
				return (shortcutOpt.isPresent()) ? shortcutOpt.get().getPhrase() : "Oof. I don't know that one";
			}
		}

		// No command was found; just a regular chatting
		return "";
	}

	private String keywordOutput(String keyword, String[] textSplit, String userId) {
		final StringBuilder builder = new StringBuilder();
		int arrLength = textSplit.length;
		String lowerKey = keyword.toLowerCase();
		LOGGER.debug("Command [" + lowerKey + "] is a keyword and performing action associated with keyword");
		switch (lowerKey) {
			case HELP:
				// List available options
				builder.append("Available commands:\\n")
						.append("!commands - list of all created commands\\n")
						.append("!joke     - request a dad joke; ability to find specific joke\\n")
						.append("!flip     - perform a coin flip; output will be either heads or tails\\n")
						.append("!roll     - perform a dice roll\\n")
						.append("!del      - delete a command\\n");
				break;
			case COMMANDS:
				builder.append("List of created commands:\\n");
				final List<Shortcut> shortcuts = findAll();
				for (Shortcut shortcut : shortcuts) {
					builder.append('!').append(shortcut.getCommand()).append("\\n");
				}
				break;
			case COIN_FLIP:
				String result = (Math.random() < 0.5) ? "Heads" : "Tails";
				builder.append(result);
				break;
			case DICE_ROLL:
				if (arrLength > 1) {
					builder.append("No need to worry about the number of dice. I gotchu breh");
				} else {
					for (int i = 0; i < DICE; i++) {
						int roll = (int) (Math.random() * 6) + 1;
						switch (roll) {
							case 1:
								builder.append(DIE_FACE_1);
								break;
							case 2:
								builder.append(DIE_FACE_2);
								break;
							case 3:
								builder.append(DIE_FACE_3);
								break;
							case 4:
								builder.append(DIE_FACE_4);
								break;
							case 5:
								builder.append(DIE_FACE_5);
								break;
							case 6:
								builder.append(DIE_FACE_6);
								break;
						}
						builder.append(' ');
					}
				}
				break;
			case DELETE:
				if (arrLength == 1) {
					builder.append("Umm...so...what do you want me to forget again?");
				} else if (arrLength > 2) {
					builder.append("Chill bruh. One at a time");
				} else {
					final String command = textSplit[1].trim();
					final Optional<Shortcut> shortcutOpt = getShortcut(command);
					if (shortcutOpt.isPresent()) {
						final String adminId = System.getenv().get(ADMIN_USER_ID);
						final Shortcut shortcut = shortcutOpt.get();
						final String shortcutUserId = shortcut.getUserId();
						if ((!StringUtils.isEmpty(shortcutUserId) && shortcut.getUserId().equalsIgnoreCase(userId))
								|| userId.equalsIgnoreCase(adminId)) {
							deleteShortcut(shortcut);
							builder.append("Poof. Gone.");
						} else {
							builder.append("Sorry breh. You're not the OP");
						}
					} else {
						builder.append("Oof. I don't know that one");
					}
				}
				break;
			case JOKE:
				if (arrLength > 2) {
					builder.append("Chill bruh. One at a time");
				} else {
					String joke = "";
					// Get a random joke or a specific joke
					if (arrLength == 1) {
						// Get a random joke
						LOGGER.debug("Search for a random joke");
						String responseString = getJoke("", false);
						joke = getJokeFromResponse(responseString, false);
					} else {
						// Get a specific joke
						final String searchTerm = textSplit[1].trim();
						LOGGER.debug("Search for a specific joke with the term [" + searchTerm + "]");
						String responseString = getJoke(searchTerm, true);
						joke = getJokeFromResponse(responseString, true);
					}

					if (StringUtils.isEmpty(joke)) {
						builder.append("Oof. Can't find dad joke. It must not exist");
					} else {
						builder.append(joke);
					}
				}
				break;
		}

		return builder.toString();
	}

	public ResponseEntity<String> postToChat(String botId, String message) {
		try {
			LOGGER.debug("Post message to group chat");
			final String jsonString = String.format(GM_JSON_STRING, botId, message);

			final URL url = new URL(GROUP_ME_URL);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Headers
			connection.setRequestMethod(HTTP_POST);
			connection.setRequestProperty(CONTENT_TYPE, APP_JSON);

			// POST request
			connection.setDoOutput(true);
			final OutputStream os = connection.getOutputStream();
			os.write(jsonString.getBytes());
			os.flush();
			os.close();

			int responseCode = connection.getResponseCode();
			connection.disconnect();
			if (responseCode > 299) {
				LOGGER.warn("Unable to post message to chat: " + message);
				return new ResponseEntity<>(HTTP_BAD_REQUEST, HttpStatus.BAD_REQUEST);
			} else {
				LOGGER.debug("Successfully posted message to chat: " + message);
				return new ResponseEntity<>(HTTP_OK, HttpStatus.OK);
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred when posting message to chat: " + message, e);
			return new ResponseEntity<>(HTTP_BAD_REQUEST, HttpStatus.BAD_REQUEST);
		}
	}

	public String getJoke(String term, boolean isSearchJoke) {
		String search = "";
		if (isSearchJoke) {
			final Map<String, String> parameters = new HashMap<>();
			parameters.put("term", term);
			final String params = getParameterString(parameters);
			search = "search?" + params;
		}

		final String jokeUrl = String.format(DAD_JOKE_URL, search);
		HttpURLConnection connection = null;
		try {
			final URL url = new URL(jokeUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(HTTP_GET);
			connection.setRequestProperty(USER_AGENT, MY_LIBRARY);
			connection.setRequestProperty(ACCEPT, APP_JSON);

			int responseCode = connection.getResponseCode();
			if (responseCode > 299) {
				return "";
			} else {
				final StringBuilder response = new StringBuilder();
				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
				}

				return response.toString();
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred retrieving the joke: ", e);
			return "";
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public String getJokeFromResponse(String response, boolean isSearchJoke) {
		final ObjectMapper mapper = new ObjectMapper();
		String joke = "";
		try {
			if (isSearchJoke) {
				final JsonNode node = mapper.readTree(response).get("results");
				final String jsonArray = String.valueOf(node);
				final List<DadJoke> dadJokes = mapper.readValue(jsonArray, new TypeReference<List<DadJoke>>() {
				});

				// NOTE: 0 is the inclusive lower limit and size is the exclusive upper limit
				int size = dadJokes.size();
				if (size > 0) {
					int randomNum = ThreadLocalRandom.current().nextInt(0, size);
					final DadJoke dadJoke = dadJokes.get(randomNum);
					joke = dadJoke.getJoke();
				}
			} else {
				final DadJoke dadJoke = mapper.readValue(response, DadJoke.class);
				joke = dadJoke.getJoke();
			}
		} catch (JsonGenerationException e) {
			LOGGER.error("JsonGenerationException: ", e);
		} catch (JsonMappingException e) {
			LOGGER.error("JsonMappingException: ", e);
		} catch (IOException e) {
			LOGGER.error("IOException: ", e);
		}

		return joke;
	}

	private String getParameterString(Map<String, String> params) {
		try {
			final StringBuilder query = new StringBuilder();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				query.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				query.append("=");
				query.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				query.append("&");
			}
			String resultString = query.toString();

			return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("An error occurred; unable to Encode parameters for URL: ", e);
			return "";
		}
	}
}
