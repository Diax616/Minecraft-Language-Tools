package dev.dixmk.language.minecraft.app;

import java.io.PrintWriter;
import java.util.Locale;

/**
 * Entry point of the application.
 *
 * Without arguments it opens the GUI, which is only available on Windows;
 * with arguments it runs the command line (see {@link LangDiffCli}).
 */
public final class Laucher {

	private Laucher() {
		throw new AssertionError("Utility class should not be instantiated");
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			System.exit(LangDiffCli.execute(args));
		}

		if (!isWindows()) {
			PrintWriter err = new PrintWriter(System.err, true, System.err.charset());
			err.println("error: the GUI is only available on Windows, run a command instead");
			LangDiffCli.printUsage(err);
			System.exit(1);
		}

		MainApp.main(args);
	}

	private static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
	}
}
