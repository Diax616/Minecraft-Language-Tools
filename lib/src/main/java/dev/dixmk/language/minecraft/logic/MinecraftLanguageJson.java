package dev.dixmk.language.minecraft.logic;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

/**
 * A Minecraft lang file: a flat JSON object at the root whose keys and values
 * are all strings. Any other shape (array, nested values, numbers, null, ...)
 * is rejected on load.
 *
 * Instances are immutable and keep the key order of the file they came from.
 */
public class MinecraftLanguageJson {

	private final Map<String, String> entries;

	private MinecraftLanguageJson(Map<String, String> entries) {
		this.entries = Collections.unmodifiableMap(entries);
	}

	public static MinecraftLanguageJson of(Map<String, String> entries) {
		return new MinecraftLanguageJson(new LinkedHashMap<>(entries));
	}

	public static MinecraftLanguageJson load(Path path) throws IOException {
		JsonElement root;
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setStrictness(Strictness.STRICT);
			root = JsonParser.parseReader(jsonReader);
			if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
				throw new LanguageJsonException(path + " is not valid JSON: trailing data after the root value");
			}
		} catch (NoSuchFileException e) {
			throw new LanguageJsonException("file not found: " + path, e);
		} catch (CharacterCodingException e) {
			throw new LanguageJsonException(path + " is not valid UTF-8", e);
		} catch (MalformedJsonException | JsonParseException e) {
			throw new LanguageJsonException(path + " is not valid JSON: " + e.getMessage(), e);
		}

		if (!root.isJsonObject()) {
			throw new LanguageJsonException(
					path + " is not a lang file: root is " + typeName(root) + ", expected an object");
		}

		Map<String, String> entries = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet()) {
			JsonElement value = entry.getValue();
			if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
				throw new LanguageJsonException(path + " is not a lang file: key '" + entry.getKey()
						+ "' holds a " + typeName(value) + ", expected a string");
			}
			entries.put(entry.getKey(), value.getAsString());
		}
		return new MinecraftLanguageJson(entries);
	}

	public void write(Path path) throws IOException {
		try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
				JsonWriter jsonWriter = new JsonWriter(writer)) {
			// A bare JsonWriter does not HTML-escape (<, >, &, =, '), which keeps
			// placeholders and markup in lang values readable.
			jsonWriter.setIndent("\t");
			jsonWriter.beginObject();
			for (Map.Entry<String, String> entry : entries.entrySet()) {
				jsonWriter.name(entry.getKey()).value(entry.getValue());
			}
			jsonWriter.endObject();
			writer.write("\n");
		}
	}

	/**
	 * The keys of this (source) file that the target does not have.
	 *
	 * @param emptyValues use empty strings instead of the source values
	 */
	public MinecraftLanguageJson missingIn(MinecraftLanguageJson target, boolean emptyValues) {
		Map<String, String> missing = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			if (!target.entries.containsKey(entry.getKey())) {
				missing.put(entry.getKey(), emptyValues ? "" : entry.getValue());
			}
		}
		return new MinecraftLanguageJson(missing);
	}

	/**
	 * This (target) file keeping only the keys that exist in the source.
	 *
	 * Values come from the target, so existing translations are preserved and
	 * only stale keys are dropped.
	 *
	 * @param sourceOrder order the result by the source instead of the target's original order
	 */
	public MinecraftLanguageJson prunedAgainst(MinecraftLanguageJson source, boolean sourceOrder) {
		Map<String, String> kept = new LinkedHashMap<>();
		if (sourceOrder) {
			for (String key : source.entries.keySet()) {
				String value = entries.get(key);
				if (value != null) {
					kept.put(key, value);
				}
			}
		} else {
			for (Map.Entry<String, String> entry : entries.entrySet()) {
				if (source.entries.containsKey(entry.getKey())) {
					kept.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return new MinecraftLanguageJson(kept);
	}

	public Map<String, String> entries() {
		return entries;
	}

	public int size() {
		return entries.size();
	}

	private static String typeName(JsonElement element) {
		if (element.isJsonNull()) {
			return "null";
		}
		if (element.isJsonArray()) {
			return "array";
		}
		if (element.isJsonObject()) {
			return "object";
		}
		if (element.getAsJsonPrimitive().isBoolean()) {
			return "boolean";
		}
		if (element.getAsJsonPrimitive().isNumber()) {
			return "number";
		}
		return "string";
	}
}
