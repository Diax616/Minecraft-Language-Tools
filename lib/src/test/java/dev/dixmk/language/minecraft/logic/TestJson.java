package dev.dixmk.language.minecraft.logic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class TestJson {
	private TestJson() {
	}

	static Path path(String name) {
		URL url = TestJson.class.getResource("/json/" + name);
		if (url == null) {
			throw new IllegalArgumentException("missing test resource: json/" + name);
		}
		try {
			return Path.of(url.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	static String read(Path path) throws IOException {
		return Files.readString(path, StandardCharsets.UTF_8);
	}

	static String read(String name) throws IOException {
		return read(path(name));
	}
}
