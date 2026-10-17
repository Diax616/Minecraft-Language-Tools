package dev.dixmk.language.minecraft.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftLanguageJsonTest {

	@TempDir
	Path dir;

	private static MinecraftLanguageJson load(String name) throws IOException {
		return MinecraftLanguageJson.load(TestJson.path(name));
	}

	private static List<String> keys(MinecraftLanguageJson json) {
		return new ArrayList<>(json.entries().keySet());
	}

	private static void assertRejected(String name, String expectedMessagePart) {
		LanguageJsonException e = assertThrows(LanguageJsonException.class, () -> load(name));
		assertTrue(e.getMessage().contains(expectedMessagePart), e.getMessage());
	}

	@Test
	void loadsFlatStringMapKeepingOrder() throws IOException {
		MinecraftLanguageJson json = load("valid1.json");

		assertEquals(7, json.size());
		List<String> keys = keys(json);
		assertEquals("gui.minepreggo.parentinfo.label.father", keys.get(0));
		assertEquals("gui.minepreggo.parentinfo.label.mother", keys.get(1));
		assertEquals("gui.minepreggo.parentinfo.value.not_biological", keys.get(6));
		assertEquals("Father:", json.entries().get("gui.minepreggo.parentinfo.label.father"));
		assertEquals(" - Not biological", json.entries().get("gui.minepreggo.parentinfo.value.not_biological"));
	}

	@Test
	void loadsFilesWithBlankLinesAndStrayWhitespace() throws IOException {
		MinecraftLanguageJson valid2 = load("valid2.json");
		assertEquals(13, valid2.size());
		assertEquals("gui.minepreggo.preggomob.main.label.not_pregnant", keys(valid2).get(0));
		assertEquals("gui.minepreggo.medical_checkup.tooltip.pregnancy_state", keys(valid2).get(12));
		assertEquals("Boobs Size: %s", valid2.entries().get("gui.minepreggo.player_main_menu.label.boob_size"));

		MinecraftLanguageJson valid3 = load("valid3.json");
		assertEquals(8, valid3.size());
		assertEquals("gui.minepreggo.request_to_player.button.stop", keys(valid3).get(0));
		assertEquals("jei.category.minepreggo.belly_shield_downgrade", keys(valid3).get(7));
	}

	@Test
	void loadsEmptyObject() throws IOException {
		assertEquals(0, load("empty.json").size());
	}

	@Test
	void rejectsMissingFile() {
		LanguageJsonException e = assertThrows(LanguageJsonException.class,
				() -> MinecraftLanguageJson.load(dir.resolve("nope.json")));
		assertTrue(e.getMessage().startsWith("file not found:"));
	}

	@Test
	void rejectsMalformedJson() {
		assertRejected("malformed.json", "is not valid JSON");
	}

	@Test
	void rejectsNonObjectRoot() {
		assertRejected("invalid1.json", "root is array");
	}

	@Test
	void rejectsNonStringValue() {
		assertRejected("invalid2.json", "key 'gui.minepreggo.parentinfo.max_children' holds a number");
	}

	@Test
	void rejectsNestedObjects() {
		assertRejected("invalid3.json", "key 'gui' holds a object");
	}

	@Test
	void writesTabsUtf8AndTrailingNewlineWithoutHtmlEscaping() throws IOException {
		Path out = dir.resolve("out.json");

		load("special.json").write(out);

		// special.json is itself in the exact output format.
		assertEquals(TestJson.read("special.json"), TestJson.read(out));
	}

	@Test
	void writesEmptyObject() throws IOException {
		Path out = dir.resolve("out.json");

		load("empty.json").write(out);

		assertEquals("{}\n", TestJson.read(out));
	}

	@Test
	void writtenFileLoadsBack() throws IOException {
		Path out = dir.resolve("out.json");
		MinecraftLanguageJson original = load("valid3.json");

		original.write(out);

		MinecraftLanguageJson reloaded = MinecraftLanguageJson.load(out);
		assertEquals(original.entries(), reloaded.entries());
		assertEquals(keys(original), keys(reloaded));
	}

	@Test
	void missingInKeepsSourceValues() throws IOException {
		MinecraftLanguageJson missing = load("diff/source.json").missingIn(load("diff/target.json"), false);

		assertEquals(List.of("gui.minepreggo.b", "gui.minepreggo.d"), keys(missing));
		assertEquals("B", missing.entries().get("gui.minepreggo.b"));
		assertEquals("D", missing.entries().get("gui.minepreggo.d"));
	}

	@Test
	void missingInCanBlankValues() throws IOException {
		MinecraftLanguageJson missing = load("diff/source.json").missingIn(load("diff/target.json"), true);

		assertEquals(List.of("gui.minepreggo.b", "gui.minepreggo.d"), keys(missing));
		assertEquals("", missing.entries().get("gui.minepreggo.b"));
		assertEquals("", missing.entries().get("gui.minepreggo.d"));
	}

	@Test
	void missingInIsEmptyWhenTargetHasEverything() throws IOException {
		MinecraftLanguageJson source = load("valid1.json");

		assertEquals(0, source.missingIn(source, false).size());
	}

	@Test
	void missingInReturnsEverythingWhenNoKeysAreShared() throws IOException {
		MinecraftLanguageJson missing = load("valid2.json").missingIn(load("valid1.json"), false);

		assertEquals(load("valid2.json").entries(), missing.entries());
	}

	@Test
	void prunedAgainstKeepsTargetValuesAndOrder() throws IOException {
		MinecraftLanguageJson pruned = load("diff/target.json").prunedAgainst(load("diff/source.json"), false);

		assertEquals(List.of("gui.minepreggo.c", "gui.minepreggo.a"), keys(pruned));
		assertEquals("tc", pruned.entries().get("gui.minepreggo.c"));
		assertEquals("ta", pruned.entries().get("gui.minepreggo.a"));
	}

	@Test
	void prunedAgainstCanFollowSourceOrder() throws IOException {
		MinecraftLanguageJson pruned = load("diff/target.json").prunedAgainst(load("diff/source.json"), true);

		assertEquals(List.of("gui.minepreggo.a", "gui.minepreggo.c"), keys(pruned));
		assertEquals("ta", pruned.entries().get("gui.minepreggo.a"));
	}

	@Test
	void prunedAgainstDropsEverythingWhenNoKeysAreShared() throws IOException {
		assertEquals(0, load("valid1.json").prunedAgainst(load("valid2.json"), false).size());
	}
}
