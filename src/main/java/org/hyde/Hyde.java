package org.hyde;

import org.hyde.Build;
import org.hyde.Clean;
import org.hyde.New;
import org.hyde.Serve;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "hyde",
    description = "Jekyll's evil twin static site generator.",
    subcommands = {New.class, Clean.class, Build.class, Serve.class})
public class Hyde implements Callable<Integer> {

    public static void main(String... args) {
    int exitCode = new CommandLine(new Hyde()).execute(args);
    if (exitCode != 0) {
        System.exit(exitCode);
    }
    }

    @Override
    public Integer call() throws Exception {
    CommandLine.usage(this, System.out);
    return 0;
    }
}
