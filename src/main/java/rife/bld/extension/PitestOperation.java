/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rife.bld.extension;

import rife.bld.BaseProject;
import rife.bld.operations.AbstractProcessOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mutation testing and coverage with <a href="https://pitest.org">PIT</a>.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class PitestOperation extends AbstractProcessOperation<PitestOperation> {
    /**
     * False constant.
     */
    protected static final String FALSE = "false";
    /**
     * True constant.
     */
    protected static final String TRUE = "true";
    private static final Logger LOGGER = Logger.getLogger(PitestOperation.class.getName());
    private static final String SOURCE_DIRS = "--sourceDirs";
    private final Map<String, String> options_ = new ConcurrentHashMap<>();
    private BaseProject project_;

    /**
     * Line arguments for child JVMs.
     *
     * @param line the line arguments
     * @return this operation instance
     */
    public PitestOperation argLine(String line) {
        if (isNotBlank(line)) {
            options_.put("--argLine", line);
        }
        return this;
    }


    /**
     * List of packages and classes which are to be considered outside the scope of mutation. Any lines of code
     * containing calls to these classes will not be mutated.
     * <p>
     * If a list is not explicitly supplied then PIT will default to a list of common logging packages as follows
     * <p>
     * <ul>
     * <li>java.util.logging</li>
     * <li>org.apache.log4j</li>
     * <li>org.slf4j</li>
     * <li>org.apache.commons.logging</li>
     * </ul>
     * <p>
     * If the feature {@code FLOGCALL} is disabled, this parameter is ignored and logging calls are also mutated.
     *
     * @param avoidCallsTo the list of packages
     * @return this operation instance
     * @see #avoidCallsTo(String...)
     */
    public PitestOperation avoidCallsTo(Collection<String> avoidCallsTo) {
        options_.put("--avoidCallsTo", String.join(",", avoidCallsTo.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * List of packages and classes which are to be considered outside the scope of mutation. Any lines of code
     * containing calls to these classes will not be mutated.
     * <p>
     * If a list is not explicitly supplied then PIT will default to a list of common logging packages as follows
     * <p>
     * <ul>
     * <li>java.util.logging</li>
     * <li>org.apache.log4j</li>
     * <li>org.slf4j</li>
     * <li>org.apache.commons.logging</li>
     * </ul>
     * <p>
     * If the feature {@code FLOGCALL} is disabled, this parameter is ignored and logging calls are also mutated.
     *
     * @param avoidCallTo one or more packages
     * @return this operation instance
     * @see #avoidCallsTo(Collection)
     */
    public PitestOperation avoidCallsTo(String... avoidCallTo) {
        return avoidCallsTo(List.of(avoidCallTo));
    }

    /**
     * List of packages and classes which are to be considered outside the scope of mutation. Any lines of code
     * containing calls to these classes will not be mutated.
     * <p>
     * If a list is not explicitly supplied then PIT will default to a list of common logging packages as follows
     * <p>
     * <ul>
     * <li>java.util.logging</li>
     * <li>org.apache.log4j</li>
     * <li>org.slf4j</li>
     * <li>org.apache.commons.logging</li>
     * </ul>
     * <p>
     * If the feature {@code FLOGCALL} is disabled, this parameter is ignored and logging calls are also mutated.
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @param path one or more paths
     * @return this operation instance
     * @see #classPath(Collection)
     */
    public PitestOperation classPath(String... path) {
        return classPath(List.of(path));
    }

    /**
     * List of packages and classes which are to be considered outside the scope of mutation. Any lines of code
     * containing calls to these classes will not be mutated.
     * <p>
     * If a list is not explicitly supplied then PIT will default to a list of common logging packages as follows
     * <p>
     * <ul>
     * <li>java.util.logging</li>
     * <li>org.apache.log4j</li>
     * <li>org.slf4j</li>
     * <li>org.apache.commons.logging</li>
     * </ul>
     * <p>
     * If the feature {@code FLOGCALL} is disabled, this parameter is ignored and logging calls are also mutated.
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @param path one or more paths
     * @return this operation instance
     * @see #classPathPaths(Collection)
     */
    public PitestOperation classPath(Path... path) {
        return classPathPaths(List.of(path));
    }

    /**
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @param path the list of paths
     * @return this operation instance
     * @see #classPath(String...)
     */
    public PitestOperation classPath(Collection<String> path) {
        options_.put("--classPath", String.join(",", path.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * List of packages and classes which are to be considered outside the scope of mutation. Any lines of code
     * containing calls to these classes will not be mutated.
     * <p>
     * If a list is not explicitly supplied then PIT will default to a list of common logging packages as follows
     * <p>
     * <ul>
     * <li>java.util.logging</li>
     * <li>org.apache.log4j</li>
     * <li>org.slf4j</li>
     * <li>org.apache.commons.logging</li>
     * </ul>
     * <p>
     * If the feature {@code FLOGCALL} is disabled, this parameter is ignored and logging calls are also mutated.
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @param path one or more paths
     * @return this operation instance
     * @see #classPathFiles(Collection)
     */
    public PitestOperation classPath(File... path) {
        return classPathFiles(List.of(path));
    }

    /**
     * File with a list of additional classpath elements (one per line).
     *
     * @param file the file
     * @return this operation instance
     */
    public PitestOperation classPathFile(String file) {
        if (isNotBlank(file)) {
            options_.put("--classPathFile", file);
        }
        return this;
    }

    /**
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @param path the list of paths
     * @return this operation instance
     * @see #classPath(File...)
     */
    public PitestOperation classPathFiles(Collection<File> path) {
        return classPath(path.stream().map(File::getAbsolutePath).toList());
    }

    /**
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @param path the list of paths
     * @return this operation instance
     * @see #classPath(Path...)
     */
    public PitestOperation classPathPaths(Collection<Path> path) {
        return classPath(path.stream().map(Path::toFile).map(File::getAbsolutePath).toList());
    }

    /**
     * Line coverage threshold below which the build will fail. This is an integer percent (0-100) that represents the
     * fraction of the project covered by the tests.
     *
     * @param threshold the threshold
     * @return this operation instance
     */
    public PitestOperation coverageThreshold(int threshold) {
        if (threshold >= 0 && threshold <= 100) {
            options_.put("--coverageThreshold", String.valueOf(threshold));
        }
        return this;
    }

    /**
     * Flag to indicate if PIT should attempt to detect the inlined code generated by the java compiler in order to
     * implement {@code finally} blocks. Each copy of the inlined code would normally be mutated separately, resulting
     * in multiple identical looking mutations. When inlined code detection is enabled PIT will attempt to spot inlined
     * code and create only a single mutation that mutates all affected instructions simultaneously.
     * <p>
     * The algorithm cannot easily distinguish between inlined copies of code, and genuine duplicate instructions on
     * the same line within a {@code finally} block.
     * <p>
     * In the case of any doubt PIT will act cautiously and assume that the code is not inlined.
     * <p>
     * This will be detected as two separate inlined instructions
     * <pre>
     * finally {
     *   int++;
     *   int++;
     * }
     * </pre>
     * But this will look confusing so PIT will assume no in-lining is taking place.
     * <pre>
     * finally {
     * &nbsp;&nbsp;int++; int++;
     * }
     * </pre>
     * This sort of pattern might not be common with integer addition, but things like string concatenation are likely
     * to produce multiple similar instructions on the same line.
     * <p>
     * Defaults to {@code true}
     *
     * @param isDetectInlinedCode {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation detectInlinedCode(boolean isDetectInlinedCode) {
        if (isDetectInlinedCode) {
            options_.put("--detectInlinedCode", TRUE);
        } else {
            options_.put("--detectInlinedCode", FALSE);
        }
        return this;
    }

    /**
     * Whether to run in dry run mode.
     * <p>
     * Defaults to {@code false}
     *
     * @param isDryRun {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation dryRun(boolean isDryRun) {
        if (isDryRun) {
            options_.put("--dryRun", TRUE);
        } else {
            options_.put("--dryRun", FALSE);
        }
        return this;
    }

    /**
     * List of globs to match against class names. Matching classes will be excluded from mutation.
     *
     * @param excludedClass the excluded classws
     * @return this operation instance
     * @see #excludedClasses(Collection)
     */
    public PitestOperation excludedClasses(String... excludedClass) {
        return excludedClasses(List.of(excludedClass));
    }

    /**
     * List of globs to match against class names. Matching classes will be excluded from mutation.
     *
     * @param excludedClasses the excluded classes
     * @return this operation instance
     * @see #excludedClasses(String...)
     */
    public PitestOperation excludedClasses(Collection<String> excludedClasses) {
        options_.put("--excludedClasses", String.join(",", excludedClasses.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * List of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories
     * are supported.
     *
     * @param excludedGroup one or more excluded groups
     * @return this operation instance
     * @see #excludedGroups(Collection)
     */
    public PitestOperation excludedGroups(String... excludedGroup) {
        return excludedGroups(List.of(excludedGroup));
    }

    /**
     * List of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories
     * are supported.
     *
     * @param excludedGroups the excluded groups
     * @return this operation instance
     * @see #excludedGroups(String...)
     */
    public PitestOperation excludedGroups(Collection<String> excludedGroups) {
        options_.put("--excludedGroups", String.join(",", excludedGroups.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * List of globs to match against method names. Methods matching the globs will be excluded from mutation.
     *
     * @param excludedMethod one or more excluded methods
     * @return this operation instance
     * @see #excludedMethods(Collection)
     */
    public PitestOperation excludedMethods(String... excludedMethod) {
        return excludedMethods(List.of(excludedMethod));
    }

    /**
     * List of globs to match against method names. Methods matching the globs will be excluded from mutation.
     *
     * @param excludedMethods the excluded methods
     * @return this operation instance
     * @see #excludedMethods(String...)
     */
    public PitestOperation excludedMethods(Collection<String> excludedMethods) {
        options_.put("--excludedMethods",
                String.join(",", excludedMethods.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * JUnit4 runners to exclude.
     *
     * @param runners the runners
     * @return this operation instance
     */
    public PitestOperation excludedRunners(String runners) {
        options_.put("--excludedRunners", runners);
        return this;
    }

    /**
     * List of globs to match against test class names. Matching tests will not be run (note if a test suite includes
     * an excluded class, then it will “leak” back in).
     *
     * @param testClasses one or more excluded tests
     * @return this operation instance
     * @see #excludedTestClasses(Collection)
     */
    public PitestOperation excludedTestClasses(String... testClasses) {
        return excludedTestClasses(List.of(testClasses));
    }

    /**
     * List of globs to match against test class names. Matching tests will not be run (note if a test suite includes
     * an excluded class, then it will “leak” back in).
     *
     * @param testClasses the excluded tests
     * @return this operation instance
     * @see #excludedTestClasses(String...)
     */
    public PitestOperation excludedTestClasses(Collection<String> testClasses) {
        options_.put("--excludedTestClasses",
                String.join(",", testClasses.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    @Override
    public void execute() throws IOException, InterruptedException, ExitStatusException {
        if (project_ == null) {
            if (LOGGER.isLoggable(Level.SEVERE) && !silent()) {
                LOGGER.severe("A project must be specified.");
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        } else {
            super.execute();
        }
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        final List<String> args = new ArrayList<>();

        if (project_ != null) {
            args.add(javaTool());
            args.add("-cp");
            args.add(String.format("%s:%s:%s:%s", new File(project_.libTestDirectory(), "*"),
                    new File(project_.libCompileDirectory(), "*"), project_.buildMainDirectory(),
                    project_.buildTestDirectory()));
            args.add("org.pitest.mutationtest.commandline.MutationCoverageReport");

            if (!options_.containsKey(SOURCE_DIRS)) {
                options_.put(SOURCE_DIRS, project_.srcDirectory().getPath());
            }

            options_.forEach((k, v) -> {
                args.add(k);
                if (!v.isEmpty()) {
                    args.add(v);
                }
            });
        }

        return args;
    }

    /**
     * Configures the operation from a {@link BaseProject}.
     *
     * @param project the project to configure the operation from
     * @since 1.5
     */
    @Override
    public PitestOperation fromProject(BaseProject project) {
        project_ = project;
        return this;
    }

    /**
     * Whether or not to dump per test line coverage data to disk.
     * <p>
     * Defaults to {@code false}
     *
     * @param jsExport {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation exportLineCoverage(boolean jsExport) {
        if (jsExport) {
            options_.put("--exportLineCoverage", TRUE);
        } else {
            options_.put("--exportLineCoverage", FALSE);
        }
        return this;
    }

    /**
     * Whether to throw an error when no mutations found.
     * <p>
     * Defaults to {@code true}
     *
     * @param isFail {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation failWhenNoMutations(boolean isFail) {
        if (isFail) {
            options_.put("--failWhenNoMutations", TRUE);
        } else {
            options_.put("--failWhenNoMutations", FALSE);
        }
        return this;
    }

    /**
     * List of features to enable/disable
     *
     * @param feature the list of features
     * @return this operation instance
     * @see #features(String...)
     */
    public PitestOperation features(Collection<String> feature) {
        options_.put("--features", String.join(",", feature.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * List of features to enable/disable
     *
     * @param feature one or more features
     * @return this operation instance
     * @see #features(Collection)
     */
    public PitestOperation features(String... feature) {
        return features(List.of(feature));
    }

    /**
     * Whether to create a full mutation matrix
     *
     * @param isFullMutationMatrix {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation fullMutationMatrix(boolean isFullMutationMatrix) {
        if (isFullMutationMatrix) {
            options_.put("--fullMutationMatrix", TRUE);
        } else {
            options_.put("--fullMutationMatrix", FALSE);
        }
        return this;
    }

    /**
     * Path to a file containing history information for incremental analysis.
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation historyInputLocation(String path) {
        if (isNotBlank(path)) {
            options_.put("--historyInputLocation", path);
        }
        return this;
    }

    /**
     * Path to a file containing history information for incremental analysis.
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation historyInputLocation(File path) {
        return historyInputLocation(path.getAbsolutePath());
    }

    /**
     * Path to a file containing history information for incremental analysis.
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation historyInputLocation(Path path) {
        return historyInputLocation(path.toFile());
    }

    /**
     * Path to write history information for incremental analysis. May be the same as
     * {@link #historyInputLocation(String)
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation historyOutputLocation(String path) {
        if (isNotBlank(path)) {
            options_.put("--historyOutputLocation", path);
        }
        return this;
    }

    /**
     * Path to write history information for incremental analysis. May be the same as
     * {@link #historyInputLocation(String)
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation historyOutputLocation(File path) {
        return historyOutputLocation(path.getAbsolutePath());
    }

    /**
     * Path to write history information for incremental analysis. May be the same as
     * {@link #historyInputLocation(String)
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation historyOutputLocation(Path path) {
        return historyOutputLocation(path.toFile());
    }

    /**
     * Indicates if the PIT should try to mutate classes on the classpath with which it was launched. If not supplied
     * this flag defaults to {@code true}. If set to {@code false} only classes found on the paths specified by the
     * {@link #classPath(String...) classPath}
     * <p>
     * Defaults to {@code true}
     *
     * @param isLaunchClasspath {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation includeLaunchClasspath(boolean isLaunchClasspath) {
        if (isLaunchClasspath) {
            options_.put("--includeLaunchClasspath", TRUE);
        } else {
            options_.put("--includeLaunchClasspath", FALSE);
        }
        return this;
    }

    /**
     * list of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories
     * are supported.
     *
     * @param includedGroup one or more included groups
     * @return this operation instance
     * @see #includedGroups(Collection)
     */
    public PitestOperation includedGroups(String... includedGroup) {
        return includedGroups(List.of(includedGroup));
    }

    /**
     * list of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories are
     * supported.
     *
     * @param includedGroups the list of included groups
     * @return this operation instance
     * @see #includedGroups(String...)
     */
    public PitestOperation includedGroups(Collection<String> includedGroups) {
        options_.put("--includedGroups", String.join(",", includedGroups.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * Test methods that should be included for challenging the mutants.
     *
     * @param testMethod the test method
     * @return this operation instance
     */
    public PitestOperation includedTestMethods(String testMethod) {
        options_.put("--includedTestMethods", testMethod);
        return this;
    }

    /**
     * Input encoding.
     * <p>
     * Default is {@code UTF-8}.
     *
     * @param encoding the encoding
     * @return this operation instance
     */
    public PitestOperation inputEncoding(String encoding) {
        if (isNotBlank(encoding)) {
            options_.put("--inputEncoding", encoding);
        }
        return this;
    }

    /*
     * Determines if a string is not blank.
     */
    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Argument string to use when PIT launches child processes. This is most commonly used to increase the amount of
     * memory available to the process, but may be used to pass any valid JVM argument.
     *
     * @param args one or moe args
     * @return this operation instance
     * @see #jvmArgs(Collection)
     */
    public PitestOperation jvmArgs(String... args) {
        return jvmArgs(List.of(args));
    }

    /**
     * Argument string to use when PIT launches child processes. This is most commonly used to increase the amount of
     * memory available to the process, but may be used to pass any valid JVM argument.
     *
     * @param args the list of args
     * @return this operation instance
     * @see #jvmArgs(String...)
     */
    public PitestOperation jvmArgs(Collection<String> args) {
        options_.put("--jvmArgs", String.join(",", args.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * The path to the java executable to be used to launch test with. If none is supplied defaults to the one
     * pointed to by {@code JAVA_HOME}.
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation jvmPath(String path) {
        if (isNotBlank(path)) {
            options_.put("--jvmPath", path);
        }
        return this;
    }

    /**
     * The path to the java executable to be used to launch test with. If none is supplied defaults to the one
     * pointed to by {@code JAVA_HOME}.
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation jvmPath(File path) {
        return jvmPath(path.getAbsolutePath());
    }

    /**
     * The path to the java executable to be used to launch test with. If none is supplied defaults to the one
     * pointed to by {@code JAVA_HOME}.
     *
     * @param path the path
     * @return this operation instance
     */
    public PitestOperation jvmPath(Path path) {
        return jvmPath(path.toFile());
    }

    /**
     * Maximum number of surviving mutants to allow without throwing an error.
     *
     * @param maxMutationsPerClass the max number
     * @return this operation instance
     */
    public PitestOperation maxMutationsPerClass(int maxMutationsPerClass) {
        options_.put("--maxMutationsPerClass", String.valueOf(maxMutationsPerClass));
        return this;
    }

    /**
     * Maximum number of surviving mutants to allow without throwing an error.
     *
     * @param maxSurviving the maximin number
     * @return this operation instance
     */
    public PitestOperation maxSurviving(int maxSurviving) {
        options_.put("--maxSurviving", String.valueOf(maxSurviving));
        return this;
    }

    /**
     * List of classpaths which should be considered to contain mutable code. If your build maintains separate output
     * directories for tests and production classes this parameter should be set to your code output directory in order
     * to avoid mutating test helper classes etc.
     * <p>
     * If no mutableCodePath is supplied PIT will default to considering anything not defined within a jar or zip file
     * as being a candidate for mutation.
     * <p>
     * PIT will always attempt not to mutate test classes even if they are defined on a mutable path.
     *
     * @param path one or one paths
     * @return this operation instance
     * @see #mutableCodePaths(Collection)
     */
    public PitestOperation mutableCodePaths(String... path) {
        return mutableCodePaths(List.of(path));
    }

    /**
     * List of classpaths which should be considered to contain mutable code. If your build maintains separate output
     * directories for tests and production classes this parameter should be set to your code output directory in order
     * to avoid mutating test helper classes etc.
     * <p>
     * If no mutableCodePath is supplied PIT will default to considering anything not defined within a jar or zip file
     * as being a candidate for mutation.
     * <p>
     * PIT will always attempt not to mutate test classes even if they are defined on a mutable path.
     *
     * @param path one or one paths
     * @return this operation instance
     * @see #mutableCodePathsPaths(Collection)
     */
    public PitestOperation mutableCodePaths(Path... path) {
        return mutableCodePathsPaths(List.of(path));
    }

    /**
     * List of classpaths which should be considered to contain mutable code. If your build maintains separate output
     * directories for tests and production classes this parameter should be set to your code output directory in order
     * to avoid mutating test helper classes etc.
     * <p>
     * If no mutableCodePath is supplied PIT will default to considering anything not defined within a jar or zip file
     * as being a candidate for mutation.
     * <p>
     * PIT will always attempt not to mutate test classes even if they are defined on a mutable path.
     *
     * @param path one or one paths
     * @return this operation instance
     * @see #mutableCodePathsFiles(Collection)
     */
    public PitestOperation mutableCodePaths(File... path) {
        return mutableCodePathsFiles(List.of(path));
    }

    /**
     * List of classpaths which should be considered to contain mutable code. If your build maintains separate output
     * directories for tests and production classes this parameter should be set to your code output directory in order
     * to avoid mutating test helper classes etc.
     * <p>
     * If no mutableCodePath is supplied PIT will default to considering anything not defined within a jar or zip file
     * as being a candidate for mutation.
     * <p>
     * PIT will always attempt not to mutate test classes even if they are defined on a mutable path.
     *
     * @param paths the list of paths
     * @return this operation instance
     * @see #mutableCodePaths(String...)
     */
    public PitestOperation mutableCodePaths(Collection<String> paths) {
        options_.put("--mutableCodePaths", String.join(",", paths.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * List of classpaths which should be considered to contain mutable code. If your build maintains separate output
     * directories for tests and production classes this parameter should be set to your code output directory in order
     * to avoid mutating test helper classes etc.
     * <p>
     * If no mutableCodePath is supplied PIT will default to considering anything not defined within a jar or zip file
     * as being a candidate for mutation.
     * <p>
     * PIT will always attempt not to mutate test classes even if they are defined on a mutable path.
     *
     * @param paths the list of paths
     * @return this operation instance
     * @see #mutableCodePaths(File...)
     */
    public PitestOperation mutableCodePathsFiles(Collection<File> paths) {
        return mutableCodePaths(paths.stream().map(File::getAbsolutePath).toList());
    }

    /**
     * List of classpaths which should be considered to contain mutable code. If your build maintains separate output
     * directories for tests and production classes this parameter should be set to your code output directory in order
     * to avoid mutating test helper classes etc.
     * <p>
     * If no mutableCodePath is supplied PIT will default to considering anything not defined within a jar or zip file
     * as being a candidate for mutation.
     * <p>
     * PIT will always attempt not to mutate test classes even if they are defined on a mutable path.
     *
     * @param paths the list of paths
     * @return this operation instance
     * @see #mutableCodePaths(Path...)
     */
    public PitestOperation mutableCodePathsPaths(Collection<Path> paths) {
        return mutableCodePaths(paths.stream().map(Path::toFile).map(File::getAbsolutePath).toList());
    }

    /**
     * Mutation engine to use.
     * <p>
     * Defaults to {@code gregor}
     *
     * @param engine the engine
     * @return this operation instance
     */
    public PitestOperation mutationEngine(String engine) {
        options_.put("--mutationEngine", engine);
        return this;
    }

    /**
     * Mutation score threshold below which the build will fail. This is an integer percent (0-100) that represents the
     * fraction of killed mutations out of all mutations.
     * <p>
     * Please bear in mind that your build may contain equivalent mutations. Careful thought must therefore be given
     * when selecting a threshold.
     *
     * @param threshold the threshold
     * @return this operation instance
     */
    public PitestOperation mutationThreshold(int threshold) {
        if (threshold >= 0 && threshold <= 100) {
            options_.put("--mutationThreshold", String.valueOf(threshold));
        }
        return this;
    }

    /**
     * Maximum number of mutations to include.
     *
     * @param size the size
     * @return this operation instance
     */
    public PitestOperation mutationUnitSize(int size) {
        options_.put("--mutationUnitSize", String.valueOf(size));
        return this;
    }

    /**
     * List of mutation operators.
     *
     * @param mutator one or more mutators
     * @return this operation instance
     * @see #mutators(Collection)
     */
    public PitestOperation mutators(String... mutator) {
        options_.put("--mutators", String.join(",", Arrays.stream(mutator).filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * List of mutation operators.
     *
     * @param mutators the list of mutators
     * @return this operation instance
     * @see #mutators(String...)
     */
    public PitestOperation mutators(Collection<String> mutators) {
        options_.put("--mutators", String.join(",", mutators.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * Returns the PIT options.
     *
     * @return the map of options
     */
    public Map<String, String> options() {
        return options_;
    }

    /**
     * Output encoding.
     * <p>
     * Default is {@code UTF-8}.
     *
     * @param encoding the encoding
     * @return this operation instance
     */
    public PitestOperation outputEncoding(String encoding) {
        if (isNotBlank(encoding)) {
            options_.put("--outputEncoding", encoding);
        }
        return this;
    }

    /**
     * A list of formats in which to write mutation results as the mutations are analysed.
     * Supported formats are {@code HTML}, {@code XML}, {@code CSV}.
     * <p>
     * Defaults to {@code HTML}.
     *
     * @param outputFormat one or more output formats
     * @return this operation instance
     * @see #outputFormatsFiles(Collection)
     */
    public PitestOperation outputFormats(File... outputFormat) {
        return outputFormatsFiles(List.of(outputFormat));
    }

    /**
     * A list of formats in which to write mutation results as the mutations are analysed.
     * Supported formats are {@code HTML}, {@code XML}, {@code CSV}.
     * <p>
     * Defaults to {@code HTML}.
     *
     * @param outputFormat one or more output formats
     * @return this operation instance
     * @see #outputFormatsPaths(Collection)
     */
    public PitestOperation outputFormats(Path... outputFormat) {
        return outputFormatsPaths(List.of(outputFormat));
    }

    /**
     * A list of formats in which to write mutation results as the mutations are analysed.
     * Supported formats are {@code HTML}, {@code XML}, {@code CSV}.
     * <p>
     * Defaults to {@code HTML}.
     *
     * @param outputFormat one or more output formats
     * @return this operation instance
     * @see #outputFormats(Collection)
     */
    public PitestOperation outputFormats(String... outputFormat) {
        return outputFormats(List.of(outputFormat));
    }

    /**
     * A list of formats in which to write mutation results as the mutations are analysed.
     * Supported formats are {@code HTML}, {@code XML}, {@code CSV}.
     * <p>
     * Defaults to {@code HTML}.
     *
     * @param outputFormats the list of output formats
     * @return this operation instance
     * @see #outputFormats(String...)
     */
    public PitestOperation outputFormats(Collection<String> outputFormats) {
        options_.put("--outputFormats", String.join(",", outputFormats.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * A list of formats in which to write mutation results as the mutations are analysed.
     * Supported formats are {@code HTML}, {@code XML}, {@code CSV}.
     * <p>
     * Defaults to {@code HTML}.
     *
     * @param outputFormats the list of output formats
     * @return this operation instance
     * @see #outputFormats(File...)
     */
    public PitestOperation outputFormatsFiles(Collection<File> outputFormats) {
        return outputFormats(outputFormats.stream().map(File::getAbsolutePath).toList());
    }

    /**
     * A list of formats in which to write mutation results as the mutations are analysed.
     * Supported formats are {@code HTML}, {@code XML}, {@code CSV}.
     * <p>
     * Defaults to {@code HTML}.
     *
     * @param outputFormats the list of output formats
     * @return this operation instance
     * @see #outputFormats(Path...)
     */
    public PitestOperation outputFormatsPaths(Collection<Path> outputFormats) {
        return outputFormats(outputFormats.stream().map(Path::toFile).map(File::getAbsolutePath).toList());

    }

    /**
     * Custom plugin properties.
     *
     * @param key   the key
     * @param value the value
     * @return this operation instance
     */
    public PitestOperation pluginConfiguration(String key, String value) {
        options_.put("--pluginConfiguration", key + '=' + value);
        return this;
    }

    /**
     * Project base.
     *
     * @param file the file
     * @return this operations instance
     */
    public PitestOperation projectBase(String file) {
        options_.put("--projectBase", file);
        return this;
    }

    /**
     * Project base.
     *
     * @param file the file
     * @return this operations instance
     */
    public PitestOperation projectBase(File file) {
        return projectBase(file.getAbsolutePath());
    }

    /**
     * Project base.
     *
     * @param file the file
     * @return this operations instance
     */
    public PitestOperation projectBase(Path file) {
        return projectBase(file.toFile());
    }


    /**
     * Output directory for the reports.
     *
     * @param dir the directory
     * @return this operation instance
     */
    public PitestOperation reportDir(String dir) {
        if (isNotBlank(dir)) {
            options_.put("--reportDir", dir);
        }
        return this;
    }

    /**
     * Output directory for the reports.
     *
     * @param dir the directory
     * @return this operation instance
     */
    public PitestOperation reportDir(File dir) {
        return reportDir(dir.getAbsolutePath());
    }

    /**
     * Output directory for the reports.
     *
     * @param dir the directory
     * @return this operation instance
     */
    public PitestOperation reportDir(Path dir) {
        return reportDir(dir.toFile());
    }

    /**
     * whether to ignore failing tests when computing coverage.
     * <p>
     * Default is {@code false}
     *
     * @param isSkipFail {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation skipFailingTests(boolean isSkipFail) {
        if (isSkipFail) {
            options_.put("--skipFailingTests", TRUE);
        } else {
            options_.put("--skipFailingTests", FALSE);
        }
        return this;
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dir one or more directories
     * @return this operation instance
     * @see #sourceDirs(Collection)
     */
    public PitestOperation sourceDirs(String... dir) {
        return sourceDirs(List.of(dir));
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dir one or more directories
     * @return this operation instance
     * @see #sourceDirsFiles(Collection)
     */
    public PitestOperation sourceDirs(File... dir) {
        return sourceDirsFiles(List.of(dir));
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dir one or more directories
     * @return this operation instance
     * @see #sourceDirsPaths(Collection)
     */
    public PitestOperation sourceDirs(Path... dir) {
        return sourceDirsPaths(List.of(dir));
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs the list of directories
     * @return this operation instance
     * @see #sourceDirs(String...)
     */
    public PitestOperation sourceDirs(Collection<String> dirs) {
        options_.put(SOURCE_DIRS, String.join(",", dirs.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs the list of directories
     * @return this operation instance
     * @see #sourceDirs(File...)
     */
    public PitestOperation sourceDirsFiles(Collection<File> dirs) {
        return sourceDirs(dirs.stream().map(File::getAbsolutePath).toList());
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs the list of directories
     * @return this operation instance
     * @see #sourceDirs(Path...)
     */
    public PitestOperation sourceDirsPaths(Collection<Path> dirs) {
        return sourceDirs(dirs.stream().map(Path::toFile).map(File::getAbsolutePath).toList());

    }

    /**
     * The classes to be mutated. This is expressed as a list of globs.
     * <p>
     * For example:
     * <p><ul>
     * <li>{@code com.myompany.*}</li>
     * <li>{@code com.mycompany.package.*, com.mycompany.packageB.Foo, com.partner.*}</li>
     * </ul></p>
     *
     * @param targetClass the list of target classes
     * @return this operation instance
     * @see #targetClasses(Collection)
     */
    public PitestOperation targetClasses(Collection<String> targetClass) {
        options_.put("--targetClasses", String.join(",", targetClass.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * The classes to be mutated. This is expressed as a list of globs.
     * <p>
     * For example:
     * <p><ul>
     * <li>{@code com.myompany.*}</li>
     * <li>{@code com.mycompany.package.*, com.mycompany.packageB.Foo, com.partner.*}</li>
     * </ul></p>
     *
     * @param targetClass one or more target classes
     * @return this operation instance
     * @see #targetClasses(String...)
     */
    public PitestOperation targetClasses(String... targetClass) {
        return targetClasses(List.of(targetClass));
    }

    /**
     * A list of globs can be supplied to this parameter to limit the tests available to be run.
     * If this parameter is not supplied then any test fixture that matched targetClasses may be used, it is however
     * recommended that this parameter is always explicitly set.
     * <p>
     * This parameter can be used to point PIT to a top level suite or suites. Custom suites such as
     * <a href="https://github.com/takari/takari-cpsuite"></a>ClassPathSuite</a> are supported.
     *
     * @param test one or more tests
     * @return this operation instance
     * @see #targetTests(Collection)
     */
    public PitestOperation targetTests(String... test) {
        return targetTests(List.of(test));
    }

    /**
     * A list of globs can be supplied to this parameter to limit the tests available to be run.
     * If this parameter is not supplied then any test fixture that matched targetClasses may be used, it is however
     * recommended that this parameter is always explicitly set.
     * <p>
     * This parameter can be used to point PIT to a top level suite or suites. Custom suites such as
     * <a href="https://github.com/takari/takari-cpsuite"></a>ClassPathSuite</a> are supported.
     *
     * @param tests the list of tests
     * @return this operation instance
     * @see #targetTests(String...)
     */
    public PitestOperation targetTests(Collection<String> tests) {
        options_.put("--targetTests", String.join(",", tests.stream().filter(this::isNotBlank).toList()));
        return this;
    }

    /**
     * Test strength score below which to throw an error.
     *
     * @param threshold the threshold
     * @return this operation instance
     */
    public PitestOperation testStrengthThreshold(int threshold) {
        options_.put("--testStrengthThreshold", String.valueOf(threshold));
        return this;
    }

    /**
     * The number of threads to use when mutation testing.
     *
     * @param threads the threads count
     * @return this operation instance
     */
    public PitestOperation threads(int threads) {
        options_.put("--threads", String.valueOf(threads));
        return this;
    }

    /**
     * Constant amount of additional time to allow a test to run for (after the application of the timeoutFactor)
     * before considering it to be stuck in an infinite loop.
     * <p>
     * Defaults to {@code 4000}
     *
     * @param factor the factor amount
     * @return this operation instance
     */
    public PitestOperation timeoutConst(int factor) {
        options_.put("--timeoutConst", String.valueOf(factor));
        return this;
    }

    /**
     * A factor to apply to the normal runtime of a test when considering if it is stuck in an infinite loop.
     * <p>
     * Defaults to {@code 1.25}
     *
     * @param factor the factor
     * @return this operation instance
     */
    public PitestOperation timeoutFactor(double factor) {
        options_.put("--timeoutFactor", String.valueOf(factor));
        return this;
    }

    /**
     * By default, PIT will create a date and time stamped folder for its output each time it is run. This can can make
     * automation difficult, so the behaviour can be suppressed by passing {@code false}.
     * <p>
     * Defaults to {@code false}
     *
     * @param isTimestamped {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation timestampedReports(boolean isTimestamped) {
        if (isTimestamped) {
            options_.put("--timestampedReports", TRUE);
        } else {
            options_.put("--timestampedReports", FALSE);
        }
        return this;
    }

    /**
     * Support large classpaths by creating a classpath jar.
     * <p>
     * Defaults to {@code false}
     *
     * @param isUseClasspathJar {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation useClasspathJar(boolean isUseClasspathJar) {
        if (isUseClasspathJar) {
            options_.put("--useClasspathJar", TRUE);
        } else {
            options_.put("--useClasspathJar", FALSE);
        }
        return this;
    }

    /**
     * Output verbose logging.
     * <p>
     * Defaults to {@code false}
     *
     * @param isVerbose {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation verbose(boolean isVerbose) {
        if (isVerbose) {
            options_.put("--verbose", TRUE);
        } else {
            options_.put("--verbose", FALSE);
        }
        return this;
    }

    /**
     * The verbosity of output.
     * <p>
     * Defaults to {@code DEFAULT}
     *
     * @param verbosity the verbosity
     * @return this operation instance
     */
    public PitestOperation verbosity(String verbosity) {
        options_.put("--verbosity", verbosity);
        return this;
    }
}
