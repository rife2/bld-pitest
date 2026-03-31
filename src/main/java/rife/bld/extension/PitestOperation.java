/*
 * Copyright 2023-2026 the original author or authors.
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import rife.bld.BaseProject;
import rife.bld.extension.tools.ClasspathTools;
import rife.bld.extension.tools.CollectionTools;
import rife.bld.extension.tools.ObjectTools;
import rife.bld.extension.tools.TextTools;
import rife.bld.operations.AbstractProcessOperation;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Mutation testing and coverage with <a href="https://pitest.org">PIT</a>.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class PitestOperation extends AbstractProcessOperation<PitestOperation> {

    private static final String FALSE = Boolean.FALSE.toString();
    private static final Logger LOGGER = Logger.getLogger(PitestOperation.class.getName());
    private static final String SOURCE_DIRS = "--sourceDirs";
    private static final String TRUE = Boolean.TRUE.toString();
    private final Map<String, String> options_ = new LinkedHashMap<>();
    private BaseProject project_;

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        Objects.requireNonNull(project_, "A project must be specified.");

        final List<String> args = new ArrayList<>();

        args.add(javaTool());
        args.add("-cp");
        args.add(buildClasspath());
        args.add("org.pitest.mutationtest.commandline.MutationCoverageReport");

        options_.putIfAbsent(SOURCE_DIRS, project_.srcDirectory().getPath());

        options_.forEach((k, v) -> {
            args.add(k);
            if (!v.isEmpty()) {
                args.add(v);
            }
        });

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.join(" ", args));
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
        project_ = Objects.requireNonNull(project, "The project must not be null");
        return this;
    }

    /**
     * Line arguments for child JVMs.
     *
     * @param line the line arguments
     * @return this operation instance
     */
    public PitestOperation argLine(String line) {
        return opt("--argLine", line);
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
     * @param values the list of packages
     * @return this operation instance
     * @see #avoidCallsTo(String...)
     */
    public final PitestOperation avoidCallsTo(Collection<String> values) {
        return optJoin("--avoidCallsTo", values);
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
     * @param values the list of packages
     * @return this operation instance
     * @see #avoidCallsTo(Collection)
     */
    public PitestOperation avoidCallsTo(String... values) {
        ObjectTools.requireAllNotEmpty(values, "avoidCallsTo values must all be non-null and non-empty");
        return avoidCallsTo(List.of(values));
    }

    /**
     * List of additional classpath entries to use when looking for tests and mutable code.
     * <p>
     * These will be used in addition to the classpath with which PIT is launched.
     *
     * @param values the list of paths
     * @return this operation instance
     * @see #classPath(String...)
     */
    public final PitestOperation classPath(Collection<String> values) {
        return optJoin("--classPath", values);
    }

    /**
     * List of additional classpath entries to use when looking for tests and mutable code.
     * <p>
     * These will be used in addition to the classpath with which PIT is launched.
     *
     * @param values one or more paths
     * @return this operation instance
     * @see #classPath(Collection)
     */
    public PitestOperation classPath(String... values) {
        ObjectTools.requireAllNotEmpty(values, "classPath values must all be non-null and non-empty");
        return classPath(List.of(values));
    }

    /**
     * List of additional classpath entries to use when looking for tests and mutable code.
     * <p>
     * These will be used in addition to the classpath with which PIT is launched.
     *
     * @param values one or more paths
     * @return this operation instance
     * @see #classPathFiles(Collection)
     */
    public PitestOperation classPath(File... values) {
        ObjectTools.requireAllNotEmpty(values, "classPath values must all be non-null");
        return classPathFiles(List.of(values));
    }

    /**
     * List of additional classpath entries to use when looking for tests and mutable code.
     * <p>
     * These will be used in addition to the classpath with which PIT is launched.
     *
     * @param values one or more paths
     * @return this operation instance
     * @see #classPathPaths(Collection)
     */
    public PitestOperation classPath(Path... values) {
        ObjectTools.requireAllNotEmpty(values, "classPath values must all be non-null");
        return classPathPaths(List.of(values));
    }

    /**
     * File with a list of additional classpath elements (one per line).
     *
     * @param file the file
     * @return this operation instance
     * @see #classPathFile(Path)
     * @see #classPathFile(File)
     */
    public PitestOperation classPathFile(String file) {
        return opt("--classPathFile", file);
    }

    /**
     * File with a list of additional classpath elements (one per line).
     *
     * @param file the file
     * @return this operation instance
     * @see #classPathFile(Path)
     * @see #classPathFile(File)
     */
    public PitestOperation classPathFile(File file) {
        Objects.requireNonNull(file, "classPathFile must not be null");
        return classPathFile(file.getAbsolutePath());
    }

    /**
     * File with a list of additional classpath elements (one per line).
     *
     * @param path the file
     * @return this operation instance
     * @see #classPathFile(String)
     * @see #classPathFile(File)
     */
    public PitestOperation classPathFile(Path path) {
        Objects.requireNonNull(path, "classPathFile must not be null");
        return classPathFile(path.toAbsolutePath().toString());
    }

    /**
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @param values the list of paths
     * @return this operation instance
     * @see #classPath(File...)
     */
    public final PitestOperation classPathFiles(Collection<File> values) {
        return optFiles("--classPath", values);
    }

    /**
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @param values the list of paths
     * @return this operation instance
     * @see #classPath(Path...)
     */
    public final PitestOperation classPathPaths(Collection<Path> values) {
        return optFiles("--classPath", CollectionTools.combinePathsToFiles(values));
    }

    /**
     * Directory to examine for configuration.
     *
     * @param dir the directory
     * @return this operation instance
     * @see #configDir(String)
     * @see #configDir(Path)
     */
    public PitestOperation configDir(File dir) {
        Objects.requireNonNull(dir, "configDir must not be null");
        return configDir(dir.getAbsolutePath());
    }

    /**
     * Directory to examine for configuration.
     *
     * @param dir the directory path
     * @return this operation instance
     * @see #configDir(File)
     * @see #configDir(String)
     */
    public PitestOperation configDir(Path dir) {
        Objects.requireNonNull(dir, "configDir must not be null");
        return configDir(dir.toFile());
    }

    /**
     * Directory to examine for configuration.
     *
     * @param dir the directory
     * @return this operation instance
     * @see #configDir(Path)
     * @see #configDir(File)
     */
    public PitestOperation configDir(String dir) {
        return opt("--configDir", dir);
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
        } else if (LOGGER.isLoggable(Level.WARNING) && !silent()) {
            LOGGER.warning("Coverage threshold must be between 0 and 100.");
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
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation detectInlinedCode(boolean value) {
        return optBool("--detectInlinedCode", value);
    }

    /**
     * Whether to run in dry run mode.
     * <p>
     * Defaults to {@code false}
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation dryRun(boolean value) {
        return optBool("--dryRun", value);
    }

    /**
     * List of globs to match against class names. Matching classes will be excluded from mutation.
     *
     * @param values the excluded classes
     * @return this operation instance
     * @see #excludedClasses(String...)
     */
    public final PitestOperation excludedClasses(Collection<String> values) {
        return optJoin("--excludedClasses", values);
    }

    /**
     * List of globs to match against class names. Matching classes will be excluded from mutation.
     *
     * @param values the excluded classes
     * @return this operation instance
     * @see #excludedClasses(Collection)
     */
    public PitestOperation excludedClasses(String... values) {
        ObjectTools.requireAllNotEmpty(values, "excludedClasses values must all be non-null and non-empty");
        return excludedClasses(List.of(values));
    }

    /**
     * List of TestNG groups/JUnit categories to exclude from mutation analysis
     * <p>
     * Note that only class level categories are supported.
     *
     * @param values the excluded groups
     * @return this operation instance
     * @see #excludedGroups(String...)
     */
    public final PitestOperation excludedGroups(Collection<String> values) {
        return optJoin("--excludedGroups", values);
    }

    /**
     * List of TestNG groups/JUnit categories to exclude from mutation analysis
     * <p>
     * Note that only class level categories are supported.
     *
     * @param values one or more excluded groups
     * @return this operation instance
     * @see #excludedGroups(Collection)
     */
    public PitestOperation excludedGroups(String... values) {
        ObjectTools.requireAllNotEmpty(values, "excludedGroups values must all be non-null and non-empty");
        return excludedGroups(List.of(values));
    }

    /**
     * List of globs to match against method names. Methods matching the globs will be excluded from mutation.
     *
     * @param values the excluded methods
     * @return this operation instance
     * @see #excludedMethods(String...)
     */
    public final PitestOperation excludedMethods(Collection<String> values) {
        return optJoin("--excludedMethods", values);
    }

    /**
     * List of globs to match against method names. Methods matching the globs will be excluded from mutation.
     *
     * @param values one or more excluded methods
     * @return this operation instance
     * @see #excludedMethods(Collection)
     */
    public PitestOperation excludedMethods(String... values) {
        ObjectTools.requireAllNotEmpty(values, "excludedMethods values must all be non-null and non-empty");
        return excludedMethods(List.of(values));
    }

    /**
     * JUnit4 runners to exclude.
     *
     * @param runners the runners
     * @return this operation instance
     */
    public PitestOperation excludedRunners(String... runners) {
        ObjectTools.requireAllNotEmpty(runners, "excludedRunners values must all be non-null and non-empty");
        return excludedRunners(List.of(runners));
    }

    /**
     * JUnit4 runners to exclude.
     *
     * @param runners the runners
     * @return this operation instance
     */
    public PitestOperation excludedRunners(Collection<String> runners) {
        return optJoin("--excludedRunners", runners);
    }

    /**
     * List of globs to match against test class names. Matching tests will not be run (note if a test suite includes
     * an excluded class, then it will "leak" back in).
     *
     * @param values the excluded tests
     * @return this operation instance
     * @see #excludedTestClasses(String...)
     */
    public final PitestOperation excludedTestClasses(Collection<String> values) {
        return optJoin("--excludedTestClasses", values);
    }

    /**
     * List of globs to match against test class names. Matching tests will not be run (note if a test suite includes
     * an excluded class, then it will "leak" back in).
     *
     * @param values one or more excluded tests
     * @return this operation instance
     * @see #excludedTestClasses(Collection)
     */
    public PitestOperation excludedTestClasses(String... values) {
        ObjectTools.requireAllNotEmpty(values, "excludedTestClasses values must all be non-null and non-empty");
        return excludedTestClasses(List.of(values));
    }

    /**
     * Whether or not to dump per test line coverage data to disk.
     * <p>
     * Defaults to {@code false}
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation exportLineCoverage(boolean value) {
        return optBool("--exportLineCoverage", value);
    }

    /**
     * Whether to create a full mutation matrix
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation failWhenNoMutations(boolean value) {
        return optBool("--failWhenNoMutations", value);
    }

    /**
     * List of features to enable/disable
     *
     * @param values one or more features
     * @return this operation instance
     * @see #features(String...)
     */
    public final PitestOperation features(Collection<String> values) {
        return optJoin("--features", values);
    }

    /**
     * List of features to enable/disable
     *
     * @param values one or more features
     * @return this operation instance
     * @see #features(Collection)
     */
    public PitestOperation features(String... values) {
        ObjectTools.requireAllNotEmpty(values, "features values must all be non-null and non-empty");
        return features(List.of(values));
    }

    /**
     * Whether to create a full mutation matrix
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation fullMutationMatrix(boolean value) {
        return optBool("--fullMutationMatrix", value);
    }

    /**
     * Path to a file containing history information for incremental analysis.
     *
     * @param path the path
     * @return this operation instance
     * @see #historyInputLocation(File)
     * @see #historyInputLocation(Path)
     */
    public PitestOperation historyInputLocation(String path) {
        return opt("--historyInputLocation", path);
    }

    /**
     * Path to a file containing history information for incremental analysis.
     *
     * @param path the path
     * @return this operation instance
     * @see #historyInputLocation(String)
     * @see #historyInputLocation(Path)
     */
    public PitestOperation historyInputLocation(File path) {
        Objects.requireNonNull(path, "historyInputLocation must not be null");
        return historyInputLocation(path.getAbsolutePath());
    }

    /**
     * Path to a file containing history information for incremental analysis.
     *
     * @param path the path
     * @return this operation instance
     * @see #historyInputLocation(String)
     * @see #historyInputLocation(File)
     */
    public PitestOperation historyInputLocation(Path path) {
        Objects.requireNonNull(path, "historyInputLocation must not be null");
        return historyInputLocation(path.toFile());
    }

    /**
     * Path to write history information for incremental analysis. May be the same as
     * {@link #historyInputLocation(String)}.
     *
     * @param path the path
     * @return this operation instance
     * @see #historyOutputLocation(File)
     * @see #historyOutputLocation(Path)
     */
    public PitestOperation historyOutputLocation(String path) {
        return opt("--historyOutputLocation", path);
    }

    /**
     * Path to write history information for incremental analysis. May be the same as
     * {@link #historyInputLocation(String)}.
     *
     * @param path the path
     * @return this operation instance
     * @see #historyOutputLocation(String)
     * @see #historyOutputLocation(Path)
     */
    public PitestOperation historyOutputLocation(File path) {
        Objects.requireNonNull(path, "historyOutputLocation must not be null");
        return historyOutputLocation(path.getAbsolutePath());
    }

    /**
     * Path to write history information for incremental analysis. May be the same as
     * {@link #historyInputLocation(String)}.
     *
     * @param path the path
     * @return this operation instance
     * @see #historyOutputLocation(String)
     * @see #historyOutputLocation(File)
     */
    public PitestOperation historyOutputLocation(Path path) {
        Objects.requireNonNull(path, "historyOutputLocation must not be null");
        return historyOutputLocation(path.toFile());
    }

    /**
     * Indicates if the PIT should try to mutate classes on the classpath with which it was launched. If not supplied
     * this flag defaults to {@code true}. If set to {@code false} only classes found on the paths specified by the
     * {@link #classPath(String...) classPath}
     * <p>
     * Defaults to {@code true}
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation includeLaunchClasspath(boolean value) {
        return optBool("--includeLaunchClasspath", value);
    }

    /**
     * List of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories are
     * supported.
     *
     * @param values the list of included groups
     * @return this operation instance
     * @see #includedGroups(String...)
     */
    public final PitestOperation includedGroups(Collection<String> values) {
        return optJoin("--includedGroups", values);
    }

    /**
     * List of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories
     * are supported.
     *
     * @param values one or more included groups
     * @return this operation instance
     */
    public PitestOperation includedGroups(String... values) {
        ObjectTools.requireAllNotEmpty(values, "includedGroups values must all be non-null and non-empty");
        return includedGroups(List.of(values));
    }

    /**
     * Test methods that should be included for challenging the mutants.
     *
     * @param testMethod the test methods
     * @return this operation instance
     */
    public PitestOperation includedTestMethods(String... testMethod) {
        ObjectTools.requireAllNotEmpty(testMethod,
                "includedTestMethods values must all be non-null and non-empty");
        return includedTestMethods(List.of(testMethod));
    }

    /**
     * Test methods that should be included for challenging the mutants.
     *
     * @param testMethod the test methods
     * @return this operation instance
     */
    public PitestOperation includedTestMethods(Collection<String> testMethod) {
        return optJoin("--includedTestMethods", testMethod);
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
        return opt("--inputEncoding", encoding);
    }

    /**
     * Argument string to use when PIT launches child processes. This is most commonly used to increase the amount of
     * memory available to the process, but may be used to pass any valid JVM argument.
     *
     * @param args the list of args
     * @return this operation instance
     * @see #jvmArgs(String...)
     */
    public final PitestOperation jvmArgs(Collection<String> args) {
        return optJoin("--jvmArgs", args);
    }

    /**
     * Argument string to use when PIT launches child processes. This is most commonly used to increase the amount of
     * memory available to the process, but may be used to pass any valid JVM argument.
     *
     * @param args one or more args
     * @return this operation instance
     * @see #jvmArgs(Collection)
     */
    public PitestOperation jvmArgs(String... args) {
        ObjectTools.requireAllNotEmpty(args, "jvmArgs values must all be non-null and non-empty");
        return jvmArgs(List.of(args));
    }

    /**
     * The path to the java executable to be used to launch test with. If none is supplied defaults to the one
     * pointed to by {@code JAVA_HOME}.
     *
     * @param path the path
     * @return this operation instance
     * @see #jvmPath(File)
     * @see #jvmPath(Path)
     */
    public PitestOperation jvmPath(String path) {
        return opt("--jvmPath", path);
    }

    /**
     * The path to the java executable to be used to launch test with. If none is supplied defaults to the one
     * pointed to by {@code JAVA_HOME}.
     *
     * @param path the path
     * @return this operation instance
     * @see #jvmPath(Path)
     * @see #jvmPath(String)
     */
    public PitestOperation jvmPath(File path) {
        Objects.requireNonNull(path, "jvmPath must not be null");
        return jvmPath(path.getAbsolutePath());
    }

    /**
     * The path to the java executable to be used to launch test with. If none is supplied defaults to the one
     * pointed to by {@code JAVA_HOME}.
     *
     * @param path the path
     * @return this operation instance
     * @see #jvmPath(File)
     * @see #jvmPath(String)
     */
    public PitestOperation jvmPath(Path path) {
        Objects.requireNonNull(path, "jvmPath must not be null");
        return jvmPath(path.toFile());
    }

    /**
     * Maximum number of surviving mutants to allow without throwing an error.
     *
     * @param max the max number
     * @return this operation instance
     */
    public PitestOperation maxMutationsPerClass(int max) {
        options_.put("--maxMutationsPerClass", String.valueOf(max));
        return this;
    }

    /**
     * Maximum number of surviving mutants to allow without throwing an error.
     *
     * @param max the maximum number
     * @return this operation instance
     */
    public PitestOperation maxSurviving(int max) {
        options_.put("--maxSurviving", String.valueOf(max));
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
     * @param values the list of values
     * @return this operation instance
     * @see #mutableCodePaths(String...)
     */
    public final PitestOperation mutableCodePaths(Collection<String> values) {
        return optJoin("--mutableCodePaths", values);
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
     * @param values one or more values
     * @return this operation instance
     * @see #mutableCodePaths(Collection)
     */
    public PitestOperation mutableCodePaths(String... values) {
        ObjectTools.requireAllNotEmpty(values, "mutableCodePaths values must all be non-null and non-empty");
        return mutableCodePaths(List.of(values));
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
     * @param values one or more values
     * @return this operation instance
     * @see #mutableCodePathsFiles(Collection)
     */
    public PitestOperation mutableCodePaths(File... values) {
        ObjectTools.requireAllNotEmpty(values, "mutableCodePaths values must all be non-null");
        return mutableCodePathsFiles(List.of(values));
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
     * @param values one or more values
     * @return this operation instance
     * @see #mutableCodePathsPaths(Collection)
     */
    public PitestOperation mutableCodePaths(Path... values) {
        ObjectTools.requireAllNotEmpty(values, "mutableCodePaths values must all be non-null");
        return mutableCodePathsPaths(List.of(values));
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
     * @param values the list of values
     * @return this operation instance
     * @see #mutableCodePaths(File...)
     */
    public final PitestOperation mutableCodePathsFiles(Collection<File> values) {
        return optFiles("--mutableCodePaths", values);
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
     * @param values the list of values
     * @return this operation instance
     * @see #mutableCodePaths(Path...)
     */
    public final PitestOperation mutableCodePathsPaths(Collection<Path> values) {
        return optFiles("--mutableCodePaths", CollectionTools.combinePathsToFiles(values));
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
        return opt("--mutationEngine", engine);
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
        } else if (LOGGER.isLoggable(Level.WARNING) && !silent()) {
            LOGGER.warning("Mutation threshold must be between 0 and 100.");
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
     * @param values one or more mutators
     * @return this operation instance
     * @see #mutators(String...)
     */
    public final PitestOperation mutators(Collection<String> values) {
        return optJoin("--mutators", values);
    }

    /**
     * List of mutation operators.
     *
     * @param values the list of mutators
     * @return this operation instance
     * @see #mutators(Collection)
     */
    public PitestOperation mutators(String... values) {
        ObjectTools.requireAllNotEmpty(values, "mutators values must all be non-null and non-empty");
        return mutators(List.of(values));
    }

    /**
     * Returns the PIT options.
     *
     * @return the map of options
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
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
        return opt("--outputEncoding", encoding);
    }

    /**
     * A list of formats in which to write mutation results as the mutations are analysed.
     * <p>
     * Supported formats are {@link OutputFormat#HTML}, {@link OutputFormat#XML}, {@link OutputFormat#CSV}.
     * <p>
     * Defaults to {@link OutputFormat#HTML}.
     *
     * @param formats one or more output formats
     * @return this operation instance
     */
    public PitestOperation outputFormats(OutputFormat... formats) {
        ObjectTools.requireAllNotEmpty(formats, "outputFormats must not be null");
        options_.put("--outputFormats",
                Arrays.stream(formats).map(Enum::name).distinct().collect(Collectors.joining(",")));
        return this;
    }

    /**
     * Custom plugin properties.
     *
     * @param configuration the configuration keys and values
     * @return this operation instance
     */
    public PitestOperation pluginConfiguration(Map<String, String> configuration) {
        ObjectTools.requireAllNotEmpty(configuration,
                "pluginConfiguration keys and values must be non-null and non-empty");
        var joined = configuration.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
        options_.put("--pluginConfiguration", joined);
        return this;
    }

    /**
     * Project base.
     *
     * @param file the file
     * @return this operations instance
     * @see #projectBase(File)
     * @see #projectBase(Path)
     */
    public PitestOperation projectBase(String file) {
        return opt("--projectBase", file);
    }

    /**
     * Project base.
     *
     * @param file the file
     * @return this operations instance
     * @see #projectBase(String)
     * @see #projectBase(Path)
     */
    public PitestOperation projectBase(File file) {
        Objects.requireNonNull(file, "projectBase must not be null");
        return projectBase(file.getAbsolutePath());
    }

    /**
     * Project base.
     *
     * @param file the file
     * @return this operations instance
     * @see #projectBase(String)
     * @see #projectBase(File)
     */
    public PitestOperation projectBase(Path file) {
        Objects.requireNonNull(file, "projectBase must not be null");
        return projectBase(file.toFile());
    }

    /**
     * Output directory for the reports.
     *
     * @param dir the directory
     * @return this operation instance
     * @see #reportDir(File)
     * @see #reportDir(Path)
     */
    public PitestOperation reportDir(String dir) {
        return opt("--reportDir", dir);
    }

    /**
     * Output directory for the reports.
     *
     * @param dir the directory
     * @return this operation instance
     * @see #reportDir(String)
     * @see #reportDir(Path)
     */
    public PitestOperation reportDir(File dir) {
        Objects.requireNonNull(dir, "reportDir must not be null");
        return reportDir(dir.getAbsolutePath());
    }

    /**
     * Output directory for the reports.
     *
     * @param dir the directory
     * @return this operation instance
     * @see #reportDir(String)
     * @see #reportDir(File)
     */
    public PitestOperation reportDir(Path dir) {
        Objects.requireNonNull(dir, "reportDir must not be null");
        return reportDir(dir.toFile());
    }

    /**
     * Whether to ignore failing tests when computing coverage.
     * <p>
     * Default is {@code false}
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation skipFailingTests(boolean value) {
        return optBool("--skipFailingTests", value);
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs one or more directories
     * @return this operation instance
     * @see #sourceDirs(Collection)
     */
    public PitestOperation sourceDirs(String... dirs) {
        ObjectTools.requireAllNotEmpty(dirs, "sourceDirs values must all be non-null and non-empty");
        return sourceDirs(List.of(dirs));
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs one or more directories
     * @return this operation instance
     * @see #sourceDirsFiles(Collection)
     */
    public PitestOperation sourceDirs(File... dirs) {
        ObjectTools.requireAllNotEmpty(dirs, "sourceDirs values must all be non-null");
        return sourceDirsFiles(List.of(dirs));
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs one or more directories
     * @return this operation instance
     * @see #sourceDirsPaths(Collection)
     */
    public PitestOperation sourceDirs(Path... dirs) {
        ObjectTools.requireAllNotEmpty(dirs, "sourceDirs values must all be non-null");
        return sourceDirsPaths(List.of(dirs));
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs the list of directories
     * @return this operation instance
     * @see #sourceDirs(String...)
     */
    public final PitestOperation sourceDirs(Collection<String> dirs) {
        return optJoin(SOURCE_DIRS, dirs);
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs the list of directories
     * @return this operation instance
     * @see #sourceDirs(File...)
     */
    public final PitestOperation sourceDirsFiles(Collection<File> dirs) {
        return optFiles(SOURCE_DIRS, dirs);
    }

    /**
     * The folder(s) containing the source code.
     *
     * @param dirs the list of directories
     * @return this operation instance
     * @see #sourceDirs(Path...)
     */
    public final PitestOperation sourceDirsPaths(Collection<Path> dirs) {
        return optFiles(SOURCE_DIRS, CollectionTools.combinePathsToFiles(dirs));
    }

    /**
     * The classes to be mutated. This is expressed as a list of globs.
     * <p>
     * For example:
     * <p><ul>
     * <li>{@code com.mycompany.*}</li>
     * <li>{@code com.mycompany.package.*, com.mycompany.packageB.Foo, com.partner.*}</li>
     * </ul></p>
     *
     * @param values the list of target classes
     * @return this operation instance
     * @see #targetClasses(String...)
     */
    public final PitestOperation targetClasses(Collection<String> values) {
        return optJoin("--targetClasses", values);
    }

    /**
     * The classes to be mutated. This is expressed as a list of globs.
     * <p>
     * For example:
     * <p><ul>
     * <li>{@code com.mycompany.*}</li>
     * <li>{@code com.mycompany.package.*, com.mycompany.packageB.Foo, com.partner.*}</li>
     * </ul></p>
     *
     * @param values one or more target classes
     * @return this operation instance
     * @see #targetClasses(Collection)
     */
    public PitestOperation targetClasses(String... values) {
        ObjectTools.requireAllNotEmpty(values, "targetClasses values must all be non-null and non-empty");
        return targetClasses(List.of(values));
    }

    /**
     * A list of globs can be supplied to this parameter to limit the tests available to be run.
     * If this parameter is not supplied then any test fixture that matched targetClasses may be used,
     * it is however recommended that this parameter is always explicitly set.
     * <p>
     * This parameter can be used to point PIT to a top level suite or suites. Custom suites such as
     * <a href="https://github.com/takari/takari-cpsuite"></a>ClassPathSuite</a> are supported.
     *
     * @param values the list of tests
     * @return this operation instance
     * @see #targetTests(String...)
     */
    public final PitestOperation targetTests(Collection<String> values) {
        return optJoin("--targetTests", values);
    }

    /**
     * A list of globs can be supplied to this parameter to limit the tests available to be run.
     * If this parameter is not supplied then any test fixture that matched targetClasses may be used, it is however
     * recommended that this parameter is always explicitly set.
     * <p>
     * This parameter can be used to point PIT to a top level suite or suites. Custom suites such as
     * <a href="https://github.com/takari/takari-cpsuite"></a>ClassPathSuite</a> are supported.
     *
     * @param values one or more tests
     * @return this operation instance
     * @see #targetTests(Collection)
     */
    public PitestOperation targetTests(String... values) {
        ObjectTools.requireAllNotEmpty(values, "targetTests values must all be non-null and non-empty");
        return targetTests(List.of(values));
    }

    /**
     * Test strength score below which to throw an error.
     * <p>
     * Threshold must be between 0 and 100.
     *
     * @param threshold the threshold
     * @return this operation instance
     */
    public PitestOperation testStrengthThreshold(int threshold) {
        if (threshold >= 0 && threshold <= 100) {
            options_.put("--testStrengthThreshold", String.valueOf(threshold));
        } else if (LOGGER.isLoggable(Level.WARNING) && !silent()) {
            LOGGER.warning("Test strength threshold must be between 0 and 100.");
        }
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
     * By default, PIT will create a date and time stamped folder for its output
     * each time it is run. This can can make automation difficult, so the
     * behaviour can be suppressed by passing {@code false}.
     * <p>
     * Defaults to {@code false}
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation timestampedReports(boolean value) {
        return optBool("--timestampedReports", value);
    }

    /**
     * Support large classpaths by creating a classpath jar.
     * <p>
     * Defaults to {@code false}
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation useClasspathJar(boolean value) {
        return optBool("--useClasspathJar", value);
    }

    /**
     * Output verbose logging.
     * <p>
     * Defaults to {@code false}
     *
     * @param value {@code true} or {@code false}
     * @return this operation instance
     */
    public PitestOperation verbose(boolean value) {
        return optBool("--verbose", value);
    }

    /**
     * The verbosity of output.
     * <p>
     * Defaults to {@code Verbosity.DEFAULT}
     *
     * @param verbosity the verbosity
     * @return this operation instance
     */
    public PitestOperation verbosity(Verbosity verbosity) {
        Objects.requireNonNull(verbosity, "verbosity must not be null");
        options_.put("--verbosity", verbosity.name());
        return this;
    }

    /**
     * Builds the complete classpath from test, compile, and provided classpath jars,
     * plus the build directories.
     *
     * @return the joined classpath string
     */
    private String buildClasspath() {
        // Combine test, compile, and provided classpath jars
        final String jarClasspath = ClasspathTools.joinClasspath(
                project_.testClasspathJars(),
                project_.compileClasspathJars(),
                project_.providedClasspathJars()
        );

        // Add build directories to the jar classpath
        return ClasspathTools.joinClasspath(
                jarClasspath,
                project_.buildMainDirectory().getAbsolutePath(),
                project_.buildTestDirectory().getAbsolutePath()
        );
    }

    // --key -> key
    private String normalizeKey(String key) {
        return key.startsWith("--") ? key.substring(2) : key;
    }

    // Stores a non-blank string option.
    private PitestOperation opt(String key, String value) {
        ObjectTools.requireNotEmpty(value, "`%s` value must not be null or empty", normalizeKey(key));
        options_.put(key, value);
        return this;
    }

    // Stores a boolean option unconditionally.
    private PitestOperation optBool(String key, boolean value) {
        options_.put(key, value ? TRUE : FALSE);
        return this;
    }

    // Converts File varargs → absolute-path collection option.
    private PitestOperation optFiles(String key, Collection<File> files) {
        ObjectTools.requireNotEmpty(files, "`%s` files must all be non-null", normalizeKey(key));
        return optJoin(key, files.stream().map(File::getAbsolutePath).toList());
    }

    // Joins a non-empty collection into a comma-separated option.
    private PitestOperation optJoin(String key, Collection<String> values) {
        ObjectTools.requireNotEmpty(values, "`%s` values must all be non-null", normalizeKey(key));
        int originalSize = values.size();

        var filtered = values.stream()
                .filter(TextTools::isNotBlank)
                .toList();

        if (!filtered.isEmpty()) {
            options_.put(key, String.join(",", filtered));
        }

        if (filtered.size() < originalSize && LOGGER.isLoggable(Level.WARNING) && !silent()) {
            LOGGER.warning("Blank values were filtered out from option `" + normalizeKey(key) + "`");
        }
        return this;
    }

    /**
     * Supported output formats.
     */
    public enum OutputFormat {
        CSV, HTML, XML
    }

    /**
     * Verbosity of output.
     */
    public enum Verbosity {
        DEFAULT,
        NO_SPINNER,
        QUIET,
        QUIET_WITH_PROGRESS,
        VERBOSE,
        VERBOSE_NO_SPINNER
    }
}