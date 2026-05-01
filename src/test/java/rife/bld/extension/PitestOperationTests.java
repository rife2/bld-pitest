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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.WebProject;
import rife.bld.blueprints.BaseProjectBlueprint;
import rife.bld.extension.testing.LoggingExtension;
import rife.bld.extension.testing.TestLogHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PitestOperationTests {

    private static final String BAR = "bar";
    private static final String FALSE = Boolean.FALSE.toString();
    private static final String FOO = "foo";
    private static final String FOOBAR = FOO + ',' + BAR;
    private static final String TRUE = Boolean.TRUE.toString();
    private static final File barFile = new File(BAR);
    private static final File fooFile = new File(FOO);
    private static final String fooBarFormat =
            String.format("%s,%s", fooFile.getAbsolutePath(), barFile.getAbsolutePath());
    private static final TestLogHandler testLogHandler = new TestLogHandler();
    @RegisterExtension
    @SuppressWarnings({"LoggerInitializedWithForeignClass", "unused"})
    private static final LoggingExtension loggingExtensions = new LoggingExtension(
            Logger.getLogger(PitestOperation.class.getName()),
            testLogHandler
    );

    @BeforeEach
    void setup() {
        testLogHandler.clear();
    }

    @Nested
    @DisplayName("Execute Tests")
    @ExtendWith(LoggingExtension.class)
    class ExecuteTests {

        @TempDir
        private Path tmpDir;

        @Test
        void execute() {
            var proj = new BaseProjectBlueprint(
                    new File("examples"),
                    "com.example",
                    "Examples",
                    "Examples");
            var op = new PitestOperation()
                    .fromProject(proj)
                    .reportDir(tmpDir)
                    .targetClasses("com.example.*")
                    .targetTests("com.example.*")
                    .verbose(false)
                    .failWhenNoMutations(false);

            assertThatCode(op::execute).doesNotThrowAnyException();
            assertThat(testLogHandler.containsMessage("Sent tests to minion"));
            assertThat(tmpDir).isNotEmptyDirectory();
        }

        @Test
        void executeConstructProcessCommandList() {
            var op = new PitestOperation().
                    fromProject(new WebProject())
                    .reportDir("outputdir")
                    .targetClasses("com.your.package.tobemutated*")
                    .targetTests("com.your.package.*")
                    .sourceDirs("parthsource");

            assertThat(String.join(" ", op.executeConstructProcessCommandList())).contains(
                    "org.pitest.mutationtest.commandline.MutationCoverageReport",
                    "--reportDir outputdir",
                    "--targetClasses com.your.package.tobemutated*",
                    "--targetTests com.your.package.*",
                    "--sourceDirs parthsource");
        }

        @Test
        void executeConstructProcessCommandListWindows() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .reportDir("c:\\mutationReports")
                    .targetClasses("example.foo.*")
                    .sourceDirs("c:\\myProject\\src")
                    .targetTests("example.foo*")
                    .threads(2)
                    .excludedMethods("hashcode", "equals");

            assertThat(String.join(" ", op.executeConstructProcessCommandList())).contains(
                    "org.pitest.mutationtest.commandline.MutationCoverageReport",
                    "--targetTests example.foo*",
                    "--threads 2",
                    "--excludedMethods hashcode,equals",
                    "--reportDir c:\\mutationReports",
                    "--targetClasses example.foo.*",
                    "--sourceDirs c:\\myProject\\src");
        }

        @Test
        void executeNoProject() {
            var op = new PitestOperation();
            assertThatCode(op::execute).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Export Line Coverage Tests")
    class ExportLineCoverageTests {

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
                    .configDir(FOO)
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
                    .outputFormats(PitestOperation.OutputFormat.CSV)
                    .pluginConfiguration(Map.of("key", "value"))
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
                    .verbosity(PitestOperation.Verbosity.DEFAULT)
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
        void configDir() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .configDir(FOO);
            assertThat(op.options().get("--configDir")).isEqualTo(FOO);
        }

        @Test
        void configDirAsFile() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .configDir(fooFile);
            assertThat(op.options().get("--configDir")).isEqualTo(fooFile.getAbsolutePath());
        }

        @Test
        void configDirAsPath() {
            var op = new PitestOperation()
                    .fromProject(new BaseProject())
                    .configDir(fooFile.toPath());
            assertThat(op.options().get("--configDir")).isEqualTo(fooFile.getAbsolutePath());
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
            var op = new PitestOperation().fromProject(new BaseProject());
            assertThatThrownBy(() -> op.coverageThreshold(101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Coverage");
        }

        @Test
        void coverageThresholdOutOfBoundsNegative() {
            var op = new PitestOperation().fromProject(new BaseProject());
            assertThatThrownBy(() -> op.coverageThreshold(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Coverage");
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
        void excludedTestClassesHasBlanks() {
            assertThatThrownBy(() ->
                    new PitestOperation()
                            .fromProject(new BaseProject())
                            .excludedTestClasses(FOO, "", BAR))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("non-null and non-empty");
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
                    .pluginConfiguration(Map.of(FOO, BAR));
            assertThat(op.options().get("--pluginConfiguration")).isEqualTo(FOO + "=" + BAR);
        }

        @Test
        void pluginConfigurationWithBlank() {
            assertThatThrownBy(() ->
                    new PitestOperation()
                            .fromProject(new Project())
                            .pluginConfiguration(Map.of(FOO, "")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("non-null and non-empty");
        }


        @Test
        void projectBase() {
            var op = new PitestOperation().projectBase(FOO);
            assertThat(op.options().get("--projectBase")).isEqualTo(FOO);
        }

        @Test
        void projectBaseAsFile() {
            var op = new PitestOperation().projectBase(fooFile);
            assertThat(op.options().get("--projectBase")).isEqualTo(fooFile.getAbsolutePath());
        }

        @Test
        void projectBaseAsPath() {
            var op = new PitestOperation().projectBase(fooFile.toPath());
            assertThat(op.options().get("--projectBase")).isEqualTo(fooFile.getAbsolutePath());
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
        void strengthThresholdOutOfBounds() {
            var op = new PitestOperation().fromProject(new BaseProject());
            assertThatThrownBy(() -> op.testStrengthThreshold(101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Test strength");

        }

        @Test
        void strengthThresholdOutOfBoundsNegative() {
            var op = new PitestOperation().fromProject(new BaseProject());
            assertThatThrownBy(() -> op.testStrengthThreshold(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Test strength");
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
        @DisplayName("Avoid Calls To Tests")
        class AvoidCallsToTests {

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
            void avoidCallsToHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .avoidCallsTo(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }
        }

        @Nested
        @DisplayName("ClassPath Tests")
        class ClassPathTests {

            private static final String CLASS_PATH = "--classPath";

            @Test
            void classPath() {
                var op = new PitestOperation().classPath(fooFile.toPath(), barFile.toPath());
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(fooBarFormat);
            }

            @Test
            void classPathAsFileArray() {
                var op = new PitestOperation().classPath(fooFile, barFile);
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(fooBarFormat);
            }

            @Test
            void classPathAsFileList() {
                var op = new PitestOperation().classPathFiles(List.of(fooFile, barFile));
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(fooBarFormat);
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
                var op = new PitestOperation().classPathPaths(List.of(fooFile.toPath(), barFile.toPath()));
                assertThat(op.options().get(CLASS_PATH)).isEqualTo(fooBarFormat);
            }

            @Test
            void classPathFile() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .classPathFile(FOO);
                assertThat(op.options().get("--classPathFile")).isEqualTo(FOO);
            }

            @Test
            void classPathFileAsFile() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .classPathFile(fooFile);
                assertThat(op.options().get("--classPathFile")).isEqualTo(fooFile.getAbsolutePath());
            }

            @Test
            void classPathFileAsPath() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .classPathFile(fooFile.toPath());
                assertThat(op.options().get("--classPathFile")).isEqualTo(fooFile.getAbsolutePath());
            }

            @Test
            void classPathFileWithBlank() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .classPathFile(""))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("null or empty");
            }

            @Test
            void classPathHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .classPath(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }

            @Test
            void classPathWithNull() {
                assertThatThrownBy(() -> new PitestOperation().classPath(fooFile, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null");
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
            void excludedClassesWithNull() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .excludedClasses(FOO, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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
            void excludedGroupsHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .excludedGroups(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }

            @Test
            void excludedGroupsWithNull() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .excludedGroups(FOO, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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
            void excludedMethodsHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .excludedMethods(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }

            @Test
            void excludedMethodsWithNull() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .excludedMethods(FOO, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }


            @Test
            void excludedRunners() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .excludedRunners(FOO);
                assertThat(op.options().get("--excludedRunners")).isEqualTo(FOO);
            }

            @Test
            void excludedRunnersAsList() {
                var op = new PitestOperation()
                        .fromProject(new BaseProject())
                        .excludedRunners(List.of(FOO, BAR));
                assertThat(op.options().get("--excludedRunners")).isEqualTo(FOO + ',' + BAR);
            }

            @Test
            void excludedRunnersWithNull() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .excludedRunners(FOO, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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

            @Test
            void excludedTestsWithNull() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .excludedTestClasses(FOO, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }
        }

        @Nested
        @DisplayName("Features Tests")
        class FeaturesTests {

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
            void featuresHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .features(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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
                op.historyInputLocation(fooFile);
                assertThat(op.options().get(historyInputLocation)).isEqualTo(fooFile.getAbsolutePath());
            }

            @Test
            void historyInputLocationAsPath() {
                var op = new PitestOperation().historyInputLocation(fooFile.toPath());
                assertThat(op.options().get(historyInputLocation)).isEqualTo(fooFile.getAbsolutePath());
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
                        .historyOutputLocation(fooFile.toPath());
                assertThat(op.options().get(historyOutputLocation)).isEqualTo(fooFile.getAbsolutePath());
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
            void includedGroupsHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .includedGroups(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }

            @Test
            void includedGroupsWithNull() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .includedGroups(FOO, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }

            @Test
            void includedTestMethods() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .includedTestMethods(FOO, BAR);
                assertThat(op.options().get("--includedTestMethods")).isEqualTo(FOO + ',' + BAR);
            }

            @Test
            void includedTestMethodsAsList() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .includedTestMethods(List.of(FOO, BAR));
                assertThat(op.options().get("--includedTestMethods")).isEqualTo(FOO + ',' + BAR);
            }

            @Test
            void includedTestMethodsWithNull() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .includedTestMethods(FOO, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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
            void jvmArgsHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .jvmArgs(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
            }

            @Test
            void jvmPath() {
                var op = new PitestOperation().jvmPath(FOO);
                assertThat(op.options().get("--jvmPath")).isEqualTo(FOO);
            }

            @Test
            void jvmPathAsFile() {
                var op = new PitestOperation().jvmPath(fooFile);
                assertThat(op.options().get("--jvmPath")).isEqualTo(fooFile.getAbsolutePath());
            }

            @Test
            void jvmPathAsPath() {
                var op = new PitestOperation().jvmPath(fooFile.toPath());
                assertThat(op.options().get("--jvmPath")).isEqualTo(fooFile.getAbsolutePath());
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
                var op = new PitestOperation().mutableCodePaths(fooFile, barFile);
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(fooBarFormat);
            }

            @Test
            void mutableCodePathsAsFileList() {
                var op = new PitestOperation().mutableCodePathsFiles(List.of(fooFile, barFile));
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(fooBarFormat);
            }

            @Test
            void mutableCodePathsAsPathArray() {
                var op = new PitestOperation().mutableCodePaths(fooFile.toPath(), barFile.toPath());
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(fooBarFormat);
            }

            @Test
            void mutableCodePathsAsPathList() {
                var op = new PitestOperation().mutableCodePathsPaths(List.of(fooFile.toPath(), barFile.toPath()));
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(fooBarFormat);
            }

            @Test
            void mutableCodePathsAsStringList() {
                var op = new PitestOperation().mutableCodePaths(List.of(FOO, BAR));
                assertThat(op.options().get(MUTABLE_CODE_PATHS)).isEqualTo(FOOBAR);
            }

            @Test
            void mutableCodePathsHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .mutableCodePaths(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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
                var op = new PitestOperation().fromProject(new BaseProject());
                assertThatThrownBy(() -> op.mutationThreshold(101))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Mutation");

            }

            @Test
            void mutationThresholdOutOfBoundsNegative() {
                var op = new PitestOperation().fromProject(new BaseProject());
                assertThatThrownBy(() -> op.mutationThreshold(-1))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Mutation");
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

            @Test
            void mutatorsHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .mutators(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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

                @ParameterizedTest
                @EnumSource(PitestOperation.OutputFormat.class)
                void outputFormats(PitestOperation.OutputFormat format) {
                    var op =
                            new PitestOperation().outputFormats(PitestOperation.OutputFormat.HTML, format);
                    var expected = new StringBuilder("HTML");
                    if (!format.equals(PitestOperation.OutputFormat.HTML)) {
                        expected.append(',').append(format.name());
                    }
                    assertThat(op.options().get(OUTPUT_FORMATS)).isEqualTo(expected.toString());
                }

                @ParameterizedTest
                @EnumSource(PitestOperation.OutputFormat.class)
                void outputFormatsEnums(PitestOperation.OutputFormat format) {
                    var op = new PitestOperation().outputFormats(format);
                    assertThat(op.options().get(OUTPUT_FORMATS)).isEqualTo(format.name());
                }
            }
        }

        @Nested
        @DisplayName("Report Tests")
        class ReportTests {

            private static final String REPORT_DIR = "--reportDir";

            @Test
            void reportDirAsFile() {
                var op = new PitestOperation().reportDir(fooFile);
                assertThat(op.options().get(REPORT_DIR)).isEqualTo(fooFile.getAbsolutePath());
            }

            @Test
            void reportDirAsPath() {
                var op = new PitestOperation().reportDir(fooFile.toPath());
                assertThat(op.options().get(REPORT_DIR)).isEqualTo(fooFile.getAbsolutePath());
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
                var op = new PitestOperation().sourceDirs(fooFile, barFile);
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(fooBarFormat);
            }

            @Test
            void sourceDirsAsFileList() {
                var op = new PitestOperation().sourceDirsFiles(List.of(fooFile, barFile));
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(fooBarFormat);
            }

            @Test
            void sourceDirsAsPathArray() {
                var op = new PitestOperation().sourceDirs(fooFile.toPath(), barFile.toPath());
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(fooBarFormat);
            }

            @Test
            void sourceDirsAsPathList() {
                var op = new PitestOperation().sourceDirsPaths(List.of(fooFile.toPath(), barFile.toPath()));
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(fooBarFormat);
            }

            @Test
            void sourceDirsAsStringList() {
                var op = new PitestOperation().sourceDirs(List.of(FOO, BAR));
                assertThat(op.options().get(SOURCE_DIRS)).isEqualTo(FOO + ',' + BAR);
            }

            @Test
            void sourceDirsHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .sourceDirs(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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
            void targetClassesHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .targetClasses(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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

            @Test
            void targetTestsHasBlanks() {
                assertThatThrownBy(() ->
                        new PitestOperation()
                                .fromProject(new BaseProject())
                                .targetTests(FOO, "", BAR))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("non-null and non-empty");
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
            void verbosityDefault() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .verbosity(PitestOperation.Verbosity.DEFAULT);
                assertThat(op.options().get("--verbosity")).isEqualTo("DEFAULT");
            }

            @Test
            void verbosityNoSpinner() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .verbosity(PitestOperation.Verbosity.NO_SPINNER);
                assertThat(op.options().get("--verbosity")).isEqualTo("NO_SPINNER");
            }

            @Test
            void verbosityQuiet() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .verbosity(PitestOperation.Verbosity.QUIET);
                assertThat(op.options().get("--verbosity")).isEqualTo("QUIET");
            }

            @Test
            void verbosityVerbose() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .verbosity(PitestOperation.Verbosity.VERBOSE);
                assertThat(op.options().get("--verbosity")).isEqualTo("VERBOSE");
            }

            @Test
            void verbosityVerboseNoSpinner() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .verbosity(PitestOperation.Verbosity.VERBOSE_NO_SPINNER);
                assertThat(op.options().get("--verbosity")).isEqualTo("VERBOSE_NO_SPINNER");
            }

            @Test
            void verbosityVerboseSpinner() {
                var op = new PitestOperation()
                        .fromProject(new Project())
                        .verbosity(PitestOperation.Verbosity.QUIET_WITH_PROGRESS);
                assertThat(op.options().get("--verbosity")).isEqualTo("QUIET_WITH_PROGRESS");
            }
        }
    }
}