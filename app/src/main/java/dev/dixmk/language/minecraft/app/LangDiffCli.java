package dev.dixmk.language.minecraft.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import dev.dixmk.language.minecraft.logic.LangDiff;
import dev.dixmk.language.minecraft.logic.LanguageJsonException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Command line of the tool, equivalent to {@code lang_diff.py}.
 *
 * Exit codes follow the script: 0 on success, 1 when a file cannot be read,
 * validated or written, and 2 when the arguments are invalid.
 */
@Command(name = "mclanguagetools", description = "Compare Minecraft lang files.",
		subcommands = { LangDiffCli.Missing.class, LangDiffCli.Prune.class })
public final class LangDiffCli implements Runnable {

	@Spec CommandSpec spec;

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "show this help message and exit")
	boolean help;

	@Override
	public void run() {
		throw new ParameterException(spec.commandLine(), "Missing required subcommand");
	}

	/** Run the command line with the given streams and return the exit code. */
	public static int execute(String[] args, PrintWriter out, PrintWriter err) {
		CommandLine commandLine = new CommandLine(new LangDiffCli());
		commandLine.setOut(out);
		commandLine.setErr(err);
		return commandLine.execute(args);
	}

	/** Run the command line on the standard streams and return the exit code. */
	public static int execute(String... args) {
		return execute(args,
				new PrintWriter(System.out, true, System.out.charset()),
				new PrintWriter(System.err, true, System.err.charset()));
	}

	/** Print the usage of the whole tool. */
	public static void printUsage(PrintWriter writer) {
		new CommandLine(new LangDiffCli()).usage(writer);
	}

	private abstract static class Task implements Callable<Integer> {

		@Spec CommandSpec spec;

		@Option(names = { "-h", "--help" }, usageHelp = true, description = "show this help message and exit")
		boolean help;

		abstract String run() throws IOException;

		@Override
		public Integer call() {
			try {
				spec.commandLine().getOut().println(run());
				return 0;
			} catch (LanguageJsonException e) {
				spec.commandLine().getErr().println("error: " + e.getMessage());
			} catch (IOException e) {
				spec.commandLine().getErr().println("error: " + e);
			}
			return 1;
		}
	}

	@Command(name = "missing", description = "write the keys the target does not have from the source")
	static final class Missing extends Task {

		@Parameters(index = "0", paramLabel = "source", description = "reference JSON (e.g. en_us.json)")
		Path source;

		@Parameters(index = "1", paramLabel = "target", description = "JSON to compare (e.g. es_mx.json)")
		Path target;

		@Parameters(index = "2", paramLabel = "output", description = "output JSON with the missing keys")
		Path output;

		@Option(names = "--empty-values", description = "write empty strings instead of the source values")
		boolean emptyValues;

		@Override
		String run() throws IOException {
			return LangDiff.missing(source, target, output, emptyValues);
		}
	}

	@Command(name = "prune", description = "write the target without the keys the source no longer has")
	static final class Prune extends Task {

		@Parameters(index = "0", paramLabel = "source", description = "reference JSON (e.g. en_us.json)")
		Path source;

		@Parameters(index = "1", paramLabel = "target", description = "JSON to prune (e.g. es_mx.json)")
		Path target;

		@Parameters(index = "2", arity = "0..1", paramLabel = "output",
				description = "output JSON; omit it when using --in-place")
		Path output;

		@Option(names = "--in-place", description = "overwrite the target instead of writing a new file")
		boolean inPlace;

		@Option(names = "--source-order", description = "order the output by the source instead of the target order")
		boolean sourceOrder;

		@Override
		String run() throws IOException {
			if (inPlace && output != null) {
				throw new ParameterException(spec.commandLine(), "--in-place takes no output file");
			}
			if (!inPlace && output == null) {
				throw new ParameterException(spec.commandLine(), "missing output file (or use --in-place)");
			}
			return LangDiff.prune(source, target, inPlace ? target : output, sourceOrder);
		}
	}
}
