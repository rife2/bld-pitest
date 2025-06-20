package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.PitestOperation;
import rife.tools.FileUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static rife.bld.dependencies.Scope.test;

import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Repository.RIFE2_RELEASES;

public class ExamplesBuild extends Project {
    public ExamplesBuild() {
        pkg = "com.example";
        name = "Examples";
        version = version(0, 1, 0);

        javaRelease = 17;

        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);

        var pitest = version(1, 20, 0);
        scope(test)
                .include(dependency("org.pitest", "pitest", pitest))
                .include(dependency("org.pitest", "pitest-command-line", pitest))
                .include(dependency("org.pitest", "pitest-junit5-plugin", version(1, 2, 3)))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 13, 1)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 13, 1)));
    }

    public static void main(String[] args) {
        // Enable detailed logging for the extensions
        var level = Level.ALL;
        var logger = Logger.getLogger("rife.bld.extension");
        var consoleHandler = new ConsoleHandler();

        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);

        new ExamplesBuild().start(args);
    }

    @BuildCommand(summary = "Run PIT mutation tests")
    public void pit() throws Exception {
        new PitestOperation()
                .fromProject(this)
                .reportDir(Path.of("reports", "mutations"))
                .targetClasses(pkg + ".*")
                .targetTests(pkg + ".*")
                .verbose(true)
                .execute();
    }
}
