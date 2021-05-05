package com.santos.util;

import java.util.Arrays;
import java.util.List;

public class Constant {
	public static final String GROUP_ME_URL = "https://api.groupme.com/v3/bots/post";
	public static final String DAD_JOKE_URL = "https://icanhazdadjoke.com/%s";
	public static final String GM_JSON_STRING = "{\"bot_id\":\"%s\",\"text\":\"%s\"}";

	public static final String HTTP_POST = "POST";
	public static final String HTTP_GET = "GET";
	public static final String HTTP_OK = "OK";
	public static final String HTTP_BAD_REQUEST = "Bad Request";

	public static final String CONTENT_TYPE = "Content-Type";
	public static final String ACCEPT = "Accept";
	public static final String USER_AGENT = "User-Agent";

	public static final String APP_JSON = "application/json";
	public static final String MY_LIBRARY = "My Library";

	public static final String BOT_ID = "BOT_ID";
	public static final String ADMIN_USER_ID = "ADMIN_USER_ID";

	public static final String HELP = "help";
	public static final String COMMANDS = "commands";
	public static final String COIN_FLIP = "flip";
	public static final String DICE_ROLL = "roll";
	public static final String DELETE = "del";
	public static final String JOKE = "joke";
	public static final List<String> KEYWORDS = Arrays.asList(HELP, COMMANDS, COIN_FLIP, DICE_ROLL, DELETE, JOKE);

	// Die faces
	public static final String DIE_FACE_1 = "\u2680";
	public static final String DIE_FACE_2 = "\u2681";
	public static final String DIE_FACE_3 = "\u2682";
	public static final String DIE_FACE_4 = "\u2683";
	public static final String DIE_FACE_5 = "\u2684";
	public static final String DIE_FACE_6 = "\u2685";

	public static final String SPACE_REGEX = "\\s+";
}
