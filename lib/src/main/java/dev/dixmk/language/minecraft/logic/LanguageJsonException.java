package dev.dixmk.language.minecraft.logic;

import java.io.IOException;

/**
 * Thrown when a file cannot be read as a Minecraft lang JSON: it is missing,
 * it is not valid JSON, or it does not have the flat string-to-string shape.
 */
public class LanguageJsonException extends IOException {

	private static final long serialVersionUID = 1L;

	public LanguageJsonException(String message) {
		super(message);
	}

	public LanguageJsonException(String message, Throwable cause) {
		super(message, cause);
	}
}
