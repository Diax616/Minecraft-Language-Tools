package dev.dixmk.language.minecraft.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LangDiffTest {

	private static final Path SOURCE = TestJson.path("diff/source.json");
	private static final Path TARGET = TestJson.path("diff/target.json");

	@TempDir
	Path dir;

	@Test
	void missingWritesSourceValuesAndSummary() throws IOException {
		Path out = dir.resolve("missing.json");

		String summary = LangDiff.missing(SOURCE, TARGET, out, false);

		assertEquals("source: 4 keys\ntarget: 3 keys\nmissing: 2 -> " + out, summary);
		assertEquals(TestJson.read("diff/expected-missing.json"), TestJson.read(out));
	}

	@Test
	void missingCanBlankValues() throws IOException {
		Path out = dir.resolve("missing.json");

		LangDiff.missing(SOURCE, TARGET, out, true);

		assertEquals(TestJson.read("diff/expected-missing-empty.json"), TestJson.read(out));
	}

	@Test
	void missingWritesEmptyObjectWhenNothingIsMissing() throws IOException {
		Path out = dir.resolve("missing.json");

		String summary = LangDiff.missing(SOURCE, SOURCE, out, false);

		assertEquals("source: 4 keys\ntarget: 4 keys\nmissing: 0 -> " + out, summary);
		assertEquals("{}\n", TestJson.read(out));
	}

	@Test
	void pruneWritesKeptKeysAndSummary() throws IOException {
		Path out = dir.resolve("pruned.json");

		String summary = LangDiff.prune(SOURCE, TARGET, out, false);

		assertEquals("source: 4 keys\ntarget: 3 keys\nkept: 2, dropped: 1 -> " + out, summary);
		assertEquals(TestJson.read("diff/expected-pruned.json"), TestJson.read(out));
	}

	@Test
	void pruneCanFollowSourceOrder() throws IOException {
		Path out = dir.resolve("pruned.json");

		LangDiff.prune(SOURCE, TARGET, out, true);

		assertEquals(TestJson.read("diff/expected-pruned-source-order.json"), TestJson.read(out));
	}

	@Test
	void pruneCanOverwriteTheTarget() throws IOException {
		Path target = Files.copy(TARGET, dir.resolve("target.json"));

		LangDiff.prune(SOURCE, target, target, false);

		assertEquals(TestJson.read("diff/expected-pruned.json"), TestJson.read(target));
	}

	@Test
	void invalidInputWritesNothing() {
		Path out = dir.resolve("missing.json");

		assertThrows(LanguageJsonException.class,
				() -> LangDiff.missing(TestJson.path("invalid1.json"), TARGET, out, false));
		assertThrows(LanguageJsonException.class,
				() -> LangDiff.prune(SOURCE, TestJson.path("invalid2.json"), out, false));

		assertFalse(Files.exists(out));
	}
}
