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

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.WebProject;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static rife.bld.extension.PitestOperation.FALSE;
import static rife.bld.extension.PitestOperation.TRUE;

class PitestOperationTests {

    private static final String BAR = "bar";
    private static final File BAR_FILE = new File(BAR);
    private static final String FOO = "foo";
    private static final String FOOBAR = FOO + ',' + BAR;
    private static final File FOO_FILE = new File(FOO);
    private static final String FOOBAR_FORMAT =
            String.format("%s,%s", FOO_FILE.getAbsolutePath(), BAR_FILE.getAbsolutePath());

    @Nested
    @DisplayName("Execute Tests")
    class ExecuteTests {

        @TempDir
        private Path tmpDir;

        @Test
        void execute() {
            var op = new PitestOperation().
                    fromProject(new WebProject())
                    .reportDir(tmpDir)
                    .targetClasses("com.example.*")
                    .targetTests("com.example.*")
                    .verbose(true)
                    .failWhenNoMutations(false)
                    .sourceDirs("examples/src");

            assertThatCode(op::execute).doesNotThrowAnyException();
            assertThat(tmpDir).isEmptyDirectory();
        }

        @Test
        void executeConstructProcessCommandList() {
            var op = new PitestOperation().
                    fromProject(new WebProject())
                    .reportDir("outputdir")
                    .targetClasses("com.your.package.tobemutated*")
                    .targetTests("com.your.package.*")
                    .sourceDirs("parthsource");

            assertThat(String.join(" ", op.executeConstructProcessCommandList())).endsWith(
                    " org.pitest.mutationtest.commandline.MutationCoverageReport " +
                            "--targetTests com.your.package.* " +
                            "--reportDir outputdir " +
                            "--targetClasses com.your.package.tobemutated* " +
                            "--sourceDirs parthsource");

            op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .reportDir("c:\\mutationReports")
                    .targetClasses("example.foo.*")
                    .sourceDirs("c:\\myProject\\src")
                    .targetTests("example.foo*")
                    .threads(2)
                    .excludedMethods("hashcode", "equals");
            assertThat(String.join(" ", op.executeConstructProcessCommandList())).endsWith(
                    "org.pitest.mutationtest.commandline.MutationCoverageReport " +
                            "--targetTests example.foo* " +
                            "--threads 2 " +
                            "--excludedMethods hashcode,equals " +
                            "--reportDir c:\\mutationReports " +
                            "--targetClasses example.foo.* " +
                            "--sourceDirs c:\\myProject\\src");
        }

        @Test
        void executeNoProject() {
            var op = new PitestOperation();
            assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        }

        @Test
        void exportLineCoverageIsFalse() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .exportLineCoverage(false);
            assertThat(op.options().get("--exportLineCoverage")).isEqualTo(FALSE);
        }

        @Test
        void exportLineCoverageIsTrue() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .exportLineCoverage(true);
            assertThat(op.options().get("--exportLineCoverage")).isEqualTo(TRUE);
        }
    }

    @Nested
    @DisplayName("Options Tests")
    class OptionsTests {

        @Test
        void argLine() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .argLine(FOO);
            assertThat(op.options().get("--argLine")).isEqualTo(FOO);
        }

        @Test
        void avoidCallsTo() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .avoidCallsTo(FOO, BAR);
            assertThat(op.options().get("--avoidCallsTo")).isEqualTo(FOOBAR);
        }

        @Test
        void avoidCallsToAsList() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .avoidCallsTo(List.of(FOO, BAR));
            assertThat(op.options().get("--avoidCallsTo")).isEqualTo(FOOBAR);
        }

        @Test
        @EnabledOnOs(OS.LINUX)
        void checkAllParameters() throws IOException {
            var args = Files.readAllLines(Paths.get("src", "test", "resources", "pitest-args.txt"));

            assertThat(args).isNotEmpty();

            var params = new PitestOperation()
                    .fromProject(new BaseProject())
                    .argLine(FOO)
                    .avoidCallsTo(FOO, BAR)
                    .classPath(FOO, BAR)
                    .classPathFile(FOO)
                    .coverageThreshold(0)
                    .detectInlinedCode(false)
                    .dryRun(false)
                    .excludedClasses("class")
                    .excludedClasses(List.of(FOO, BAR))
                    .excludedGroups("group")
                    .excludedGroups(List.of(FOO, BAR))
                    .excludedMethods("method")
                    .excludedMethods(List.of(FOO, BAR))
                    .excludedRunners("runners")
                    .excludedTestClasses("test")
                    .exportLineCoverage(true)
                    .failWhenNoMutations(true)
                    .features("feature")
                    .fullMutationMatrix(true)
                    .historyInputLocation("inputLocation")
                    .historyOutputLocation("outputLocation")
                    .includeLaunchClasspath(true)
                    .includedGroups("group")
                    .includedTestMethods("method")
                    .inputEncoding("encoding")
                    .jvmArgs("-XX:+UnlogregckDiagnosticVMOptions")
                    .jvmPath("path")
                    .maxMutationsPerClass(3)
                    .maxSurviving(1)
                    .mutableCodePaths("codePaths")
                    .mutationEngine("engine")
                    .mutationThreshold(0)
                    .mutationUnitSize(1)
                    .mutators(List.of(FOO, BAR))
                    .outputEncoding("encoding")
                    .outputFormats("json")
                    .pluginConfiguration("key", "value")
                    .projectBase("base")
                    .reportDir("dir")
                    .skipFailingTests(true)
                    .targetClasses("class")
                    .targetTests("test")
                    .testStrengthThreshold(0)
                    .threads(0)
                    .timeoutConst(0)
                    .timeoutFactor(0)
                    .timestampedReports(true)
                    .useClasspathJar(true)
                    .verbose(true)
                    .verbosity("default")
                    .executeConstructProcessCommandList();

            try (var softly = new AutoCloseableSoftAssertions()) {
                for (var p : args) {
                    var found = false;
                    for (var a : params) {
                        if (a.startsWith(p)) {
                            found = true;
                            break;
                        }
                    }
                    softly.assertThat(found).as("%s not found", p).isTrue();
                }
            }
        }

        @Test
        void coverageThreshold() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .coverageThreshold(3);
            assertThat(op.options().get("--coverageThreshold")).isEqualTo("3");
        }

        @Test
        void coverageThresholdOutOfBounds() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .coverageThreshold(101);
            assertThat(op.options().get("--coverageThreshold")).isNull();
        }

        @Test
        void coverageThresholdOutOfBoundsNegative() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .coverageThreshold(-1);
            assertThat(op.options().get("--coverageThreshold")).isNull();
        }

        @Test
        void detectInlinedCodeIsFalse() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .detectInlinedCode(false);
            assertThat(op.options().get("--detectInlinedCode")).isEqualTo(FALSE);
        }

        @Test
        void detectInlinedCodeIsTrue() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .detectInlinedCode(true);
            assertThat(op.options().get("--detectInlinedCode")).isEqualTo(TRUE);
        }

        @Test
        void features() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .features(FOO, BAR);
            assertThat(op.options().get("--features")).isEqualTo(FOOBAR);
        }

        @Test
        void featuresAsList() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .features(List.of(FOO, BAR));
            assertThat(op.options().get("--features")).isEqualTo(FOOBAR);
        }


        @Test
        void inputEncoding() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .inputEncoding(FOO);
            assertThat(op.options().get("--inputEncoding")).isEqualTo(FOO);
        }

        @Test
        void maxSurviving() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .maxSurviving(1);
            assertThat(op.options().get("--maxSurviving")).isEqualTo("1");
        }

        @Test
        void pluginConfiguration() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .pluginConfiguration(FOO, BAR);
            assertThat(op.options().get("--pluginConfiguration")).isEqualTo(FOO + "=" + BAR);
        }

        @Test
        void projectBase() {
            var op = new PitestOperation().projectBase(FOO);
            assertThat(op.options().get("--projectBase")).isEqualTo(FOO);
        }

        @Test
        void projectBaseAsFile() {
            var op = new PitestOperation().projectBase(FOO_FILE);
            assertThat(op.options().get("--projectBase")).isEqualTo(FOO_FILE.getAbsolutePath());
        }

        @Test
        void projectBaseAsPath() {
            var op = new PitestOperation().projectBase(FOO_FILE.toPath());
            assertThat(op.options().get("--projectBase")).isEqualTo(FOO_FILE.getAbsolutePath());
        }

        @Test
        void skipFailingTestsIsFalse() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .skipFailingTests(false);
            assertThat(op.options().get("--skipFailingTests")).isEqualTo(FALSE);
        }

        @Test
        void skipFailingTestsIsTrue() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .skipFailingTests(true);
            assertThat(op.options().get("--skipFailingTests")).isEqualTo(TRUE);
        }

        @Test
        void strengthThreshold() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .testStrengthThreshold(6);
            assertThat(op.options().get("--testStrengthThreshold")).isEqualTo("6");
        }

        @Test
        void threads() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .threads(3);
            assertThat(op.options().get("--threads")).isEqualTo("3");
        }

        @Test
        void timestampedReportsIsFalse() {
            var op = new PitestOperation()
                    .fromProject(new Project())
                    .timestampedReports(false);
            assertThat(op.options().get("--timestampedReports")).isEqualTo(FALSE);
        }

        @Test
        void timestampedReportsIsTrue() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .timestampedReports(true);
            assertThat(op.options().get("--timestampedReports")).isEqualTo(TRUE);
        }

        @Nested
        @DisplayName("ClassPath Tests")
        class ClassPathTests {

            private static final String CLASS_PATH = "--classPath";

            @Test
            void classPath() {
                var op = new PitestOperation().classPath(FOO_FILE.toPath(), BAR_FILE.toPath());
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void classPathAsFile() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .classPathFile(FOO);
                assertThat(op.options().get("--classPathFile")).isEqualTo(FOO);
            }

            @Test
            void classPathAsFileArray() {
                var op = new PitestOperation().classPath(FOO_FILE, BAR_FILE);
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void classPathAsFileList() {
                var op = new PitestOperation().classPathFiles(List.of(FOO_FILE, BAR_FILE));
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void classPathAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .classPath(List.of(FOO, BAR));
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(FOOBAR);
            }

            @Test
            void classPathAsStringList() {
                var op = new PitestOperation().classPathPaths(List.of(FOO_FILE.toPath(), BAR_FILE.toPath()));
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void useClasspathJarIsFalse() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .useClasspathJar(false);
                assertThat(op.options().get("--useClasspathJar")).isEqualTo(FALSE);
            }

            @Test
            void useClasspathJarIsTrue() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .useClasspathJar(true);
                assertThat(op.options().get("--useClasspathJar")).isEqualTo(TRUE);
            }
        }

        @Nested
        @DisplayName("Excluded Tests")
        class ExcludedTests {

            @Test
            void excludedClasses() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .excludedClasses(FOO, BAR);
                assertThat(op.options().get("--excludedClasses")).isEqualTo(FOOBAR);
            }

            @Test
            void excludedClassesAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .excludedClasses(Set.of(FOO, BAR));
                assertThat(op.options().get("--excludedClasses")).contains(",");
            }

            @Test
            void excludedGroups() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .excludedGroups(FOO, BAR);
                assertThat(op.options().get("--excludedGroups")).isEqualTo(FOOBAR);
            }

            @Test
            void excludedGroupsAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .excludedGroups(List.of(FOO, BAR));
                assertThat(op.options().get("--excludedGroups")).isEqualTo(FOOBAR);
            }

            @Test
            void excludedMethods() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .excludedMethods(FOO, BAR);
                assertThat(op.options().get("--excludedMethods")).isEqualTo(FOOBAR);

                op = new PitestOperation()
                        .fromProject(new Project())
                        .excludedMethods(List.of(FOO, BAR));
                assertThat(op.options().get("--excludedMethods")).isEqualTo(FOOBAR);
            }

            @Test
            void excludedRunners() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .excludedRunners(FOO);
                assertThat(op.options().get("--excludedRunners")).isEqualTo(FOO);
            }

            @Test
            void excludedTests() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .excludedTestClasses(FOO, BAR);
                assertThat(op.options().get("--excludedTestClasses")).isEqualTo(FOOBAR);
            }

            @Test
            void excludedTestsAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .excludedTestClasses(List.of(FOO, BAR));
                assertThat(op.options().get("--excludedTestClasses")).isEqualTo(FOOBAR);
            }
        }

        @Nested
        @DisplayName("History Location Tests")
        class HistoryLocationTests {

            private static final String historyInputLocation = "--historyInputLocation";
            private static final String historyOutputLocation = "--historyOutputLocation";
            private final PitestOperation op = new PitestOperation().fromProject(new WebProject());

            @Test
            void historyInputLocationAsFile() {
                op.historyInputLocation(FOO_FILE);
                assertThat(op.options().get(historyInputLocation)).isEqualTo(FOO_FILE.getAbsolutePath());
            }

            @Test
            void historyInputLocationAsPath() {
                var op = new PitestOperation().historyInputLocation(FOO_FILE.toPath());
                assertThat(op.options().get(historyInputLocation)).isEqualTo(FOO_FILE.getAbsolutePath());
            }

            @Test
            void historyInputLocationAsString() {
                op.historyInputLocation(FOO);
                assertThat(op.options().get(historyInputLocation)).isEqualTo(FOO);
            }

            @Test
            void historyOutputLocationAsPath() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .historyOutputLocation(FOO_FILE.toPath());
                assertThat(op.options().get(historyOutputLocation)).isEqualTo(FOO_FILE.getAbsolutePath());
            }

            @Test
            void historyOutputLocationAsString() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .historyOutputLocation(FOO);
                assertThat(op.options().get(historyOutputLocation)).isEqualTo(FOO);
            }
        }

        @Nested
        @DisplayName("Include Test")
        class IncludeTest {

            @Test
            void includeLaunchClasspathIsFalse() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .includeLaunchClasspath(false);
                assertThat(op.options().get("--includeLaunchClasspath")).isEqualTo(FALSE);
            }

            @Test
            void includeLaunchClasspathIsTrue() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .includeLaunchClasspath(true);
                assertThat(op.options().get("--includeLaunchClasspath")).isEqualTo(TRUE);
            }

            @Test
            void includedGroups() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .includedGroups(FOO, BAR);
                assertThat(op.options().get("--includedGroups")).isEqualTo(FOOBAR);
            }

            @Test
            void includedGroupsAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .includedGroups(List.of(FOO, BAR));
                assertThat(op.options().get("--includedGroups")).isEqualTo(FOOBAR);
            }

            @Test
            void includedTestMethods() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .includedTestMethods(FOO);
                assertThat(op.options().get("--includedTestMethods")).isEqualTo(FOO);
            }
        }

        @Nested
        @DisplayName("JVM Tests")
        class JvmTests {

            @Test
            void jvmArgs() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .jvmArgs(FOO, BAR);
                assertThat(op.options().get("--jvmArgs")).isEqualTo(FOOBAR);
            }

            @Test
            void jvmArgsAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .jvmArgs(List.of(FOO, BAR));
                assertThat(op.options().get("--jvmArgs")).isEqualTo(FOOBAR);
            }

            @Test
            void jvmPath() {
                var op = new PitestOperation().jvmPath(FOO);
                assertThat(op.options().get("--jvmPath")).isEqualTo(FOO);
            }

            @Test
            void jvmPathAsFile() {
                var op = new PitestOperation().jvmPath(FOO_FILE);
                assertThat(op.options().get("--jvmPath")).isEqualTo(FOO_FILE.getAbsolutePath());
            }

            @Test
            void jvmPathAsPath() {
                var op = new PitestOperation().jvmPath(FOO_FILE.toPath());
                assertThat(op.options().get("--jvmPath")).isEqualTo(FOO_FILE.getAbsolutePath());
            }
        }

        @Nested
        @DisplayName("Mutable Code Paths Tests")
        class MutableCodePathsTests {

            private static final String MUTABLE_CODE_PATHS = "--mutableCodePaths";

            @Test
            void mutableCodePaths() {
                var op = new PitestOperation().mutableCodePaths(FOO, BAR);
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(FOOBAR);
            }

            @Test
            void mutableCodePathsAsFileArray() {
                var op = new PitestOperation().mutableCodePaths(FOO_FILE, BAR_FILE);
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void mutableCodePathsAsFileList() {
                var op = new PitestOperation().mutableCodePathsFiles(List.of(FOO_FILE, BAR_FILE));
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void mutableCodePathsAsPathArray() {
                var op = new PitestOperation().mutableCodePaths(FOO_FILE.toPath(), BAR_FILE.toPath());
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void mutableCodePathsAsPathList() {
                var op = new PitestOperation().mutableCodePathsPaths(List.of(FOO_FILE.toPath(), BAR_FILE.toPath()));
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void mutableCodePathsAsStringList() {
                var op = new PitestOperation().mutableCodePaths(List.of(FOO, BAR));
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(FOOBAR);
            }
        }

        @Nested
        @DisplayName("Mutation Tests")
        class MutationTests {

            @Test
            void failWhenNoMutationsIsFalse() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .failWhenNoMutations(false);
                assertThat(op.options().get("--failWhenNoMutations")).isEqualTo(FALSE);
            }

            @Test
            void failWhenNoMutationsIsTrue() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .failWhenNoMutations(true);
                assertThat(op.options().get("--failWhenNoMutations")).isEqualTo(TRUE);
            }

            @Test
            void fullMutationMatrixIsFalse() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .fullMutationMatrix(false);
                assertThat(op.options().get("--fullMutationMatrix")).isEqualTo(FALSE);
            }

            @Test
            void fullMutationMatrixIsTrue() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .fullMutationMatrix(true);
                assertThat(op.options().get("--fullMutationMatrix")).isEqualTo(TRUE);
            }

            @Test
            void maxMutationsPerClass() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .maxMutationsPerClass(12);
                assertThat(op.options().get("--maxMutationsPerClass")).isEqualTo("12");
            }

            @Test
            void mutationEngine() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .mutationEngine(FOO);
                assertThat(op.options().get("--mutationEngine")).isEqualTo(FOO);
            }

            @Test
            void mutationThreshold() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .mutationThreshold(3);
                assertThat(op.options().get("--mutationThreshold")).isEqualTo("3");
            }

            @Test
            void mutationThresholdOutOfBounds() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .mutationThreshold(101);
                assertThat(op.options().get("--mutationThreshold")).isNull();
            }

            @Test
            void mutationThresholdOutOfBoundsNegative() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .mutationThreshold(-1);
                assertThat(op.options().get("--mutationThreshold")).isNull();
            }

            @Test
            void mutationUnitSize() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .mutationUnitSize(2);
                assertThat(op.options().get("--mutationUnitSize")).isEqualTo("2");
            }

            @Test
            void mutators() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .mutators(FOO, BAR);
                assertThat(op.options().get("--mutators")).isEqualTo(FOOBAR);
            }

            @Test
            void mutatorsAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .mutators(List.of(FOO, BAR));
                assertThat(op.options().get("--mutators")).isEqualTo(FOOBAR);
            }
        }

        @Nested
        @DisplayName("Output Tests")
        class OutputTests {

            private static final String OUTPUT_FORMATS = "--outputFormats";

            @Test
            void outputEncoding() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .outputEncoding(FOO);
                assertThat(op.options().get("--outputEncoding")).isEqualTo(FOO);
            }

            @Nested
            @DisplayName("Output Formats Tests")
            class OutputFormatsTests {

                @Test
                void outputFormats() {
                    var op = new PitestOperation().outputFormats(FOO, BAR);
                    assertThat(op.options().get(OUTPUT_FORMATS)).isEqualTo(FOO + ',' + BAR);
                }

                @Test
                void outputFormatsAsFileArray() {
                    var op = new PitestOperation().outputFormats(FOO_FILE, BAR_FILE);
                    assertThat(op.options().get(OUTPUT_FORMATS)).isEqualTo(FOOBAR_FORMAT);
                }

                @Test
                void outputFormatsAsFileList() {
                    var op = new PitestOperation().outputFormatsFiles(List.of(FOO_FILE, BAR_FILE));
                    assertThat(op.options().get(OUTPUT_FORMATS)).isEqualTo(FOOBAR_FORMAT);
                }

                @Test
                void outputFormatsAsPathArray() {
                    var op = new PitestOperation().outputFormats(FOO_FILE.toPath(), BAR_FILE.toPath());
                    assertThat(op.options().get(OUTPUT_FORMATS)).isEqualTo(FOOBAR_FORMAT);
                }

                @Test
                void outputFormatsAsPathList() {
                    var op = new PitestOperation().outputFormatsPaths(List.of(FOO_FILE.toPath(), BAR_FILE.toPath()));
                    assertThat(op.options().get(OUTPUT_FORMATS)).isEqualTo(FOOBAR_FORMAT);
                }

                @Test
                void outputFormatsAsStringList() {
                    var op = new PitestOperation().outputFormats(List.of(FOO, BAR));
                    assertThat(op.options().get(OUTPUT_FORMATS)).isEqualTo(FOO + ',' + BAR);
                }
            }
        }

        @Nested
        @DisplayName("Report Tests")
        class ReportTests {

            private static final String REPORT_DIR = "--reportDir";

            @Test
            void reportDirAsFile() {
                var op = new PitestOperation().reportDir(FOO_FILE);
                assertThat(op.options().get(REPORT_DIR)).isEqualTo(FOO_FILE.getAbsolutePath());
            }

            @Test
            void reportDirAsPath() {
                var op = new PitestOperation().reportDir(FOO_FILE.toPath());
                assertThat(op.options().get(REPORT_DIR)).isEqualTo(FOO_FILE.getAbsolutePath());
            }

            @Test
            void reportDirAsString() {
                var op = new PitestOperation().reportDir(FOO);
                assertThat(op.options().get(REPORT_DIR)).isEqualTo(FOO);
            }
        }

        @Nested
        @DisplayName("Source Directories Tests")
        class SourceDirectoriesTests {

            private static final String SOURCE_DIRS = "--sourceDirs";

            @Test
            void sourceDirs() {
                var op = new PitestOperation().sourceDirs(FOO, BAR);
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(FOO + ',' + BAR);
            }

            @Test
            void sourceDirsAsFileArray() {
                var op = new PitestOperation().sourceDirs(FOO_FILE, BAR_FILE);
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void sourceDirsAsFileList() {
                var op = new PitestOperation().sourceDirsFiles(List.of(FOO_FILE, BAR_FILE));
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void sourceDirsAsPathArray() {
                var op = new PitestOperation().sourceDirs(FOO_FILE.toPath(), BAR_FILE.toPath());
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void sourceDirsAsPathList() {
                var op = new PitestOperation().sourceDirsPaths(List.of(FOO_FILE.toPath(), BAR_FILE.toPath()));
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(FOOBAR_FORMAT);
            }

            @Test
            void sourceDirsAsStringList() {
                var op = new PitestOperation().sourceDirs(List.of(FOO, BAR));
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(FOO + ',' + BAR);
            }
        }

        @Nested
        @DisplayName("Target Tests")
        class TargetTests {

            @Test
            void targetClasses() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .targetClasses(FOO, BAR);
                assertThat(op.options().get("--targetClasses")).isEqualTo(FOOBAR);
            }

            @Test
            void targetClassesAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .targetClasses(List.of(FOO, BAR));
                assertThat(op.options().get("--targetClasses")).isEqualTo(FOOBAR);
            }

            @Test
            void targetTests() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .targetTests(FOO, BAR);
                assertThat(op.options().get("--targetTests")).isEqualTo(FOOBAR);
            }

            @Test
            void targetTestsAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .targetTests(List.of(FOO, BAR));
                assertThat(op.options().get("--targetTests")).isEqualTo(FOOBAR);
            }
        }

        @Nested
        @DisplayName("Timeout Tests")
        class TimeoutTests {

            @Test
            void timeoutConst() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .timeoutConst(300);
                assertThat(op.options().get("--timeoutConst")).isEqualTo("300");
            }

            @Test
            void timeoutFactor() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .timeoutFactor(5.25);
                assertThat(op.options().get("--timeoutFactor")).isEqualTo("5.25");
            }
        }

        @Nested
        @DisplayName("Verbose Tests")
        class VerboseTests {

            @Test
            void verboseIsFalse() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .verbose(false);
                assertThat(op.options().get("--verbose")).isEqualTo(FALSE);
            }

            @Test
            void verboseIsTrue() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .verbose(true);
                assertThat(op.options().get("--verbose")).isEqualTo(TRUE);
            }

            @Test
            void verbosity() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .verbosity(FOO);
                assertThat(op.options().get("--verbosity")).isEqualTo(FOO);
            }
        }
    }
}
