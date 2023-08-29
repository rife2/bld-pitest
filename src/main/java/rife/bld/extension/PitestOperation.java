/*
 * Copyright 2023 the original author or authors.
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
     * Source directories command line option.
     */
    protected static final String SOURCE_DIRS = "--sourceDirs";
    /**
     * True constant.
     */
    protected static final String TRUE = "true";
    private static final Logger LOGGER = Logger.getLogger(PitestOperation.class.getName());
    /**
     * The PIT options.
     */
    protected final Map<String, String> options = new ConcurrentHashMap<>();
    private BaseProject project_;

    /**
     * Line arguments for child JVMs.
     */
    public PitestOperation argLine(String line) {
        options.put("--argLine", line);
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
     * @see #avoidCallsTo(String...)
     */
    public PitestOperation avoidCallsTo(Collection<String> avoidCallsTo) {
        options.put("--avoidCallsTo", String.join(",", avoidCallsTo));
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
     * @see #avoidCallsTo(Collection)
     */
    public PitestOperation avoidCallsTo(String... avoidCallTo) {
        options.put("--avoidCallsTo", String.join(",", avoidCallTo));
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
     * @see #classPath(Collection)
     */
    public PitestOperation classPath(String... path) {
        options.put("--classPath", String.join(",", path));
        return this;
    }

    /**
     * Additional classpath entries to use when looking for tests and mutable code.
     *
     * @see #classPath(String...)
     */
    public PitestOperation classPath(Collection<String> paths) {
        options.put("--classPath", String.join(",", paths));
        return this;
    }

    /**
     * File with a list of additional classpath elements (one per line).
     */
    public PitestOperation classPathFile(String file) {
        options.put("--classPathFile", file);
        return this;
    }

    /**
     * Line coverage threshold below which the build will fail. This is an integer percent (0-100) that represents the
     * fraction of the project covered by the tests.
     */
    public PitestOperation coverageThreshold(int threshold) {
        if (threshold >= 0 && threshold <= 100) {
            options.put("--coverageThreshold", String.valueOf(threshold));
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
     */
    public PitestOperation detectInlinedCode(boolean isDetectInlinedCode) {
        if (isDetectInlinedCode) {
            options.put("--detectInlinedCode", TRUE);
        } else {
            options.put("--detectInlinedCode", FALSE);
        }
        return this;
    }

    /**
     * List of globs to match against class names. Matching classes will be excluded from mutation.
     *
     * @see #excludedClasses(Collection)
     */
    public PitestOperation excludedClasses(String... excludedClass) {
        options.put("--excludedClasses", String.join(",", excludedClass));
        return this;
    }

    /**
     * List of globs to match against class names. Matching classes will be excluded from mutation.
     *
     * @see #excludedClasses(String...)
     */
    public PitestOperation excludedClasses(Collection<String> excludedClasses) {
        options.put("--excludedClasses", String.join(",", excludedClasses));
        return this;
    }

    /**
     * List of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories
     * are supported.
     *
     * @see #excludedGroups(Collection)
     */
    public PitestOperation excludedGroups(String... excludedGroup) {
        options.put("--excludedGroups", String.join(",", excludedGroup));
        return this;
    }

    /**
     * List of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories
     * are supported.
     *
     * @see #excludedGroups(String...)
     */
    public PitestOperation excludedGroups(Collection<String> excludedGroups) {
        options.put("--excludedGroups", String.join(",", excludedGroups));
        return this;
    }

    /**
     * List of globs to match against method names. Methods matching the globs will be excluded from mutation.
     *
     * @see #excludedMethods(Collection)
     */
    public PitestOperation excludedMethods(String... excludedMethod) {
        options.put("--excludedMethods", String.join(",", excludedMethod));
        return this;
    }

    /**
     * List of globs to match against method names. Methods matching the globs will be excluded from mutation.
     *
     * @see #excludedMethods(String...)
     */
    public PitestOperation excludedMethods(Collection<String> excludedMethods) {
        options.put("--excludedMethods", String.join(",", excludedMethods));
        return this;
    }

    /**
     * List of globs to match against test class names. Matching tests will not be run (note if a test suite includes
     * an excluded class, then it will “leak” back in).
     *
     * @see #excludedTests(Collection)
     */
    public PitestOperation excludedTests(String... excludedTest) {
        options.put("--excludedTests", String.join(",", excludedTest));
        return this;
    }

    /**
     * List of globs to match against test class names. Matching tests will not be run (note if a test suite includes
     * an excluded class, then it will “leak” back in).
     *
     * @see #excludedTests(String...)
     */
    public PitestOperation excludedTests(Collection<String> excludedTests) {
        options.put("--excludedTests", String.join(",", excludedTests));
        return this;
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        if (project_ == null) {
            LOGGER.severe("A project must be specified.");
        } else if (!options.containsKey(SOURCE_DIRS)) {
            options.put(SOURCE_DIRS, project_.srcDirectory().getPath());
        }

        final List<String> args = new ArrayList<>();
        args.add(javaTool());

        args.add("-cp");
        args.add(String.format("%s:%s:%s:%s", Path.of(project_.libTestDirectory().getPath(), "*"),
                Path.of(project_.libCompileDirectory().getPath(), "*"), project_.buildMainDirectory(),
                project_.buildTestDirectory()));
        args.add("org.pitest.mutationtest.commandline.MutationCoverageReport");

        options.forEach((k, v) -> {
            args.add(k);
            if (!v.isEmpty()) {
                args.add(v);
            }
        });

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
     */
    public PitestOperation exportLineCoverage(boolean jsExport) {
        if (jsExport) {
            options.put("--exportLineCoverage", TRUE);
        } else {
            options.put("--exportLineCoverage", FALSE);
        }
        return this;
    }

    /**
     * Whether to throw an error when no mutations found.
     * <p>
     * Defaults to {@code true}
     */
    public PitestOperation failWhenNoMutations(boolean isFail) {
        if (isFail) {
            options.put("--failWhenNoMutations", TRUE);
        } else {
            options.put("--failWhenNoMutations", FALSE);
        }
        return this;
    }

    /**
     * List of features to enable/disable
     *
     * @see #features(String...)
     */
    public PitestOperation features(Collection<String> feature) {
        options.put("--features", String.join(",", feature));
        return this;
    }

    /**
     * List of features to enable/disable
     *
     * @see #features(Collection)
     */
    public PitestOperation features(String... feature) {
        options.put("--features", String.join(",", feature));
        return this;
    }

    /**
     * Path to a file containing history information for incremental analysis.
     */
    public PitestOperation historyInputLocation(String path) {
        options.put("--historyInputLocation", path);
        return this;
    }

    /**
     * Path to write history information for incremental analysis. May be the same as
     * {@link #historyInputLocation(String) historyInputLocation}.
     */
    public PitestOperation historyOutputLocation(String path) {
        options.put("--historyOutputLocation", path);
        return this;
    }

    /**
     * Indicates if the PIT should try to mutate classes on the classpath with which it was launched. If not supplied
     * this flag defaults to {@code true}. If set to {@code false} only classes found on the paths specified by the
     * {@link #classPath(String...)} classPath} option will be considered.
     * <p>
     * Defaults to {@code true}
     */
    public PitestOperation includeLaunchClasspath(boolean isLaunchClasspath) {
        if (isLaunchClasspath) {
            options.put("--includeLaunchClasspath", TRUE);
        } else {
            options.put("--includeLaunchClasspath", FALSE);
        }
        return this;
    }

    /**
     * list of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories
     * are supported.
     *
     * @see #includedGroups(Collection)
     */
    public PitestOperation includedGroups(String... includedGroup) {
        options.put("--includedGroups", String.join(",", includedGroup));
        return this;
    }

    /**
     * list of TestNG groups/JUnit categories to include in mutation analysis. Note that only class level categories are
     * supported.
     *
     * @see #includedGroups(String...)
     */
    public PitestOperation includedGroups(Collection<String> includedGroups) {
        options.put("--includedGroups", String.join(",", includedGroups));
        return this;
    }

    /**
     * Argument string to use when PIT launches child processes. This is most commonly used to increase the amount of
     * memory available to the process, but may be used to pass any valid JVM argument.
     *
     * @see #jvmArgs(Collection)
     */
    public PitestOperation jvmArgs(String... args) {
        options.put("--jvmArgs", String.join(",", args));
        return this;
    }

    /**
     * Argument string to use when PIT launches child processes. This is most commonly used to increase the amount of
     * memory available to the process, but may be used to pass any valid JVM argument.
     *
     * @see #jvmArgs(String...)
     */
    public PitestOperation jvmArgs(Collection<String> args) {
        options.put("--jvmArgs", String.join(",", args));
        return this;
    }

    /**
     * The path to the java executable to be used to launch test with. If none is supplied defaults to the one
     * pointed to by {@code JAVA_HOME}.
     */
    public PitestOperation jvmPath(String path) {
        options.put("--jvmPath", path);
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
     * @see #mutableCodePaths(Collection)
     */
    public PitestOperation mutableCodePaths(String... path) {
        options.put("--mutableCodePaths", String.join(",", path));
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
     * @see #mutableCodePaths(String...)
     */
    public PitestOperation mutableCodePaths(Collection<String> paths) {
        options.put("--mutableCodePaths", String.join(",", paths));
        return this;
    }

    /**
     * Mutation score threshold below which the build will fail. This is an integer percent (0-100) that represents the
     * fraction of killed mutations out of all mutations.
     * <p>
     * Please bear in mind that your build may contain equivalent mutations. Careful thought must therefore be given
     * when selecting a threshold.
     */
    public PitestOperation mutationThreshold(int threshold) {
        if (threshold >= 0 && threshold <= 100) {
            options.put("--mutationThreshold", String.valueOf(threshold));
        }
        return this;
    }

    /**
     * List of mutation operators.
     *
     * @see #mutators(Collection)
     */
    public PitestOperation mutators(String... mutator) {
        options.put("--mutators", String.join(",", mutator));
        return this;
    }

    /**
     * List of mutation operators.
     *
     * @see #mutators(String...)
     */
    public PitestOperation mutators(Collection<String> mutators) {
        options.put("--mutators", String.join(",", mutators));
        return this;
    }

    /**
     * Output encoding.
     * <p>
     * Default is {@code UTF-8}.
     */
    public PitestOperation outputEncoding(String encoding) {
        options.put("--outputEncoding", encoding);
        return this;
    }

    /**
     * Comma separated list of formats in which to write mutation results as the mutations are analysed.
     * Supported formats are {@code HTML}, {@code XML}, {@code CSV}.
     * <p>
     * Defaults to {@code HTML}.
     *
     * @see #outputFormats(Collection)
     */
    public PitestOperation outputFormats(String... outputFormat) {
        options.put("--outputFormats", String.join(",", outputFormat));
        return this;
    }

    /**
     * Comma separated list of formats in which to write mutation results as the mutations are analysed.
     * Supported formats are {@code HTML}, {@code XML}, {@code CSV}.
     * <p>
     * Defaults to {@code HTML}.
     *
     * @see #outputFormats(String...)
     */
    public PitestOperation outputFormats(Collection<String> outputFormats) {
        options.put("--outputFormats", String.join(",", outputFormats));
        return this;
    }

    /**
     * Output directory for the reports.
     */
    public PitestOperation reportDir(String dir) {
        options.put("--reportDir", dir);
        return this;
    }

    /**
     * whether to ignore failing tests when computing coverage.
     * <p>
     * Default is {@code false}
     */
    public PitestOperation skipFailingTests(boolean isSkipFail) {
        if (isSkipFail) {
            options.put("--skipFailingTests", TRUE);
        } else {
            options.put("--skipFailingTests", FALSE);
        }
        return this;
    }

    /**
     * The folder(s) containing the source code.
     *
     * @see #sourceDirs(Collection)
     */
    public PitestOperation sourceDirs(String... dir) {
        options.put(SOURCE_DIRS, String.join(",", dir));
        return this;
    }

    /**
     * The folder(s) containing the source code.
     *
     * @see #sourceDirs(String...)
     */
    public PitestOperation sourceDirs(Collection<String> dirs) {
        options.put(SOURCE_DIRS, String.join(",", dirs));
        return this;
    }

    /**
     * The classes to be mutated. This is expressed as a list of globs.
     * <p>
     * For example
     * <p><ul>
     * <li>{@code com.myompany.*}</li>
     * <li>{@code com.mycompany.package.*, com.mycompany.packageB.Foo, com.partner.*}</li>
     * </ul></p>
     *
     * @see #targetClasses(Collection)
     */
    public PitestOperation targetClasses(Collection<String> targetClass) {
        options.put("--targetClasses", String.join(",", targetClass));
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
     * @see #targetClasses(String...)
     */
    public PitestOperation targetClasses(String... targetClass) {
        options.put("--targetClasses", String.join(",", targetClass));
        return this;
    }

    /**
     * A comma separated list of globs can be supplied to this parameter to limit the tests available to be run.
     * If this parameter is not supplied then any test fixture that matched targetClasses may be used, it is however
     * recommended that this parameter is always explicitly set.
     * <p>
     * This parameter can be used to point PIT to a top level suite or suites. Custom suites such as
     * <a href="https://github.com/takari/takari-cpsuite"></a>ClassPathSuite</a> are supported.
     *
     * @see #targetTests(Collection)
     */
    public PitestOperation targetTests(String... test) {
        options.put("--targetTests", String.join(",", test));
        return this;
    }

    /**
     * A comma separated list of globs can be supplied to this parameter to limit the tests available to be run.
     * If this parameter is not supplied then any test fixture that matched targetClasses may be used, it is however
     * recommended that this parameter is always explicitly set.
     * <p>
     * This parameter can be used to point PIT to a top level suite or suites. Custom suites such as
     * <a href="https://github.com/takari/takari-cpsuite"></a>ClassPathSuite</a> are supported.
     *
     * @see #targetTests(String...)
     */
    public PitestOperation targetTests(Collection<String> tests) {
        options.put("--targetTests", String.join(",", tests));
        return this;
    }

    /**
     * The number of threads to use when mutation testing.
     */
    public PitestOperation threads(int threads) {
        options.put("--threads", String.valueOf(threads));
        return this;
    }

    /**
     * Constant amount of additional time to allow a test to run for (after the application of the timeoutFactor)
     * before considering it to be stuck in an infinite loop.
     * <p>
     * Defaults to {@code 4000}
     */
    public PitestOperation timeoutConst(int factor) {
        options.put("--timeoutConst", String.valueOf(factor));
        return this;
    }

    /**
     * A factor to apply to the normal runtime of a test when considering if it is stuck in an infinite loop.
     * <p>
     * Defaults to {@code 1.25}
     */
    public PitestOperation timeoutFactor(double factor) {
        options.put("--timeoutFactor", String.valueOf(factor));
        return this;
    }

    /**
     * By default PIT will create a date and time stamped folder for its output each time it is run. This can can make
     * automation difficult, so the behaviour can be suppressed by passing {@code false}.
     * <p>
     * Defaults to {@code false}
     */
    public PitestOperation timestampedReports(boolean isTimestamped) {
        if (isTimestamped) {
            options.put("--timestampedReports", TRUE);
        } else {
            options.put("--timestampedReports", FALSE);
        }
        return this;
    }

    /**
     * Support large classpaths by creating a classpath jar.
     * <p>
     * Defaults to {@code false}
     */
    public PitestOperation useClasspathJar(boolean isUseClasspathJar) {
        if (isUseClasspathJar) {
            options.put("--useClasspathJar", TRUE);
        } else {
            options.put("--useClasspathJar", FALSE);
        }
        return this;
    }

    /**
     * Output verbose logging.
     * <p>
     * Defaults to {@code false}
     */
    public PitestOperation verbose(boolean isVerbose) {
        if (isVerbose) {
            options.put("--verbose", TRUE);
        } else {
            options.put("--verbose", FALSE);
        }
        return this;
    }
}