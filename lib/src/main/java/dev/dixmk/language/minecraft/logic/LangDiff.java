package dev.dixmk.language.minecraft.logic;

import java.io.IOException;
import java.nio.file.Path;

public final class LangDiff {
	private LangDiff() {
		throw new AssertionError("Utility class should not be instantiated");
	}

	public static String missing(Path sourcePath, Path targetPath, Path output, boolean emptyValues)
			throws IOException {
		MinecraftLanguageJson source = MinecraftLanguageJson.load(sourcePath);
		MinecraftLanguageJson target = MinecraftLanguageJson.load(targetPath);

		MinecraftLanguageJson missing = source.missingIn(target, emptyValues);
		missing.write(output);

		return counts(source, target) + "missing: " + missing.size() + " -> " + output;
	}

	public static String prune(Path sourcePath, Path targetPath, Path output, boolean sourceOrder)
			throws IOException {
		MinecraftLanguageJson source = MinecraftLanguageJson.load(sourcePath);
		MinecraftLanguageJson target = MinecraftLanguageJson.load(targetPath);

		MinecraftLanguageJson kept = target.prunedAgainst(source, sourceOrder);
		kept.write(output);

		return counts(source, target) + "kept: " + kept.size() + ", dropped: "
				+ (target.size() - kept.size()) + " -> " + output;
	}

	private static String counts(MinecraftLanguageJson source, MinecraftLanguageJson target) {
		return "source: " + source.size() + " keys\n" + "target: " + target.size() + " keys\n";
	}
}
