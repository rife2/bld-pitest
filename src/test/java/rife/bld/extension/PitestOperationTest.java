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

import org.junit.jupiter.api.Test;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.WebProject;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static rife.bld.extension.PitestOperation.FALSE;
import static rife.bld.extension.PitestOperation.TRUE;

class PitestOperationTest {
    private static final String AS_LIST = "as list";
    private final static String BAR = "bar";
    private final static String FOO = "foo";
    private final static String FOOBAR = FOO + ',' + BAR;

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

        op = new PitestOperation()
                .fromProject(new Project())
                .avoidCallsTo(List.of(FOO, BAR));
        assertThat(op.options().get("--avoidCallsTo")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
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

        for (var p : args) {
            var found = false;
            for (var a : params) {
                if (a.startsWith(p)) {
                    found = true;
                    break;
                }
            }
            assertThat(found).as(p + " not found.").isTrue();
        }
    }

    @Test
    void classPath() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .classPath(FOO, BAR);
        assertThat(op.options().get("--classPath")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .classPath(List.of(FOO, BAR));
        assertThat(op.options().get("--classPath")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
    void classPathFile() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .classPathFile(FOO);
        assertThat(op.options().get("--classPathFile")).isEqualTo(FOO);
    }

    @Test
    void coverageThreshold() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .coverageThreshold(3);
        assertThat(op.options().get("--coverageThreshold")).isEqualTo("3");

        op = new PitestOperation()
                .fromProject(new BaseProject())
                .coverageThreshold(101);
        assertThat(op.options().get("--coverageThreshold")).isNull();
    }

    @Test
    void detectInlinedCode() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .detectInlinedCode(true);
        assertThat(op.options().get("--detectInlinedCode")).isEqualTo(TRUE);

        op = new PitestOperation()
                .fromProject(new Project())
                .detectInlinedCode(false);
        assertThat(op.options().get("--detectInlinedCode")).isEqualTo(FALSE);
    }

    @Test
    void excludedClasses() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .excludedClasses(FOO, BAR);
        assertThat(op.options().get("--excludedClasses")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .excludedClasses(Set.of(FOO, BAR));
        assertThat(op.options().get("--excludedClasses")).as("as set").contains(FOO).contains(BAR).contains(",");
    }

    @Test
    void excludedGroups() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .excludedGroups(FOO, BAR);
        assertThat(op.options().get("--excludedGroups")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .excludedGroups(List.of(FOO, BAR));
        assertThat(op.options().get("--excludedGroups")).as(AS_LIST).isEqualTo(FOOBAR);
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
        assertThat(op.options().get("--excludedMethods")).as(AS_LIST).isEqualTo(FOOBAR);
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

        op = new PitestOperation()
                .fromProject(new Project())
                .excludedTestClasses(List.of(FOO, BAR));
        assertThat(op.options().get("--excludedTestClasses")).as("as list").isEqualTo(FOOBAR);
    }

    @Test
    void execute() throws IOException {
        var tmpDir = Files.createTempDirectory("bld-pitest-");
        tmpDir.toFile().deleteOnExit();
        var op = new PitestOperation().
                fromProject(new WebProject())
                .reportDir(tmpDir.toAbsolutePath().toString())
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
        assertThat(String.join(" ", op.executeConstructProcessCommandList())).as("mutationReports").endsWith(
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
    void exportLineCoverage() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .exportLineCoverage(true);
        assertThat(op.options().get("--exportLineCoverage")).isEqualTo(TRUE);

        op = new PitestOperation()
                .fromProject(new Project())
                .exportLineCoverage(false);
        assertThat(op.options().get("--exportLineCoverage")).isEqualTo(FALSE);
    }

    @Test
    void failWhenNoMutations() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .failWhenNoMutations(true);
        assertThat(op.options().get("--failWhenNoMutations")).isEqualTo(TRUE);

        op = new PitestOperation()
                .fromProject(new Project())
                .failWhenNoMutations(false);
        assertThat(op.options().get("--failWhenNoMutations")).isEqualTo(FALSE);
    }

    @Test
    void features() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .features(FOO, BAR);
        assertThat(op.options().get("--features")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .features(List.of(FOO, BAR));
        assertThat(op.options().get("--features")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
    void fullMutationMatrix() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .fullMutationMatrix(true);
        assertThat(op.options().get("--fullMutationMatrix")).isEqualTo(TRUE);
    }

    @Test
    void historyInputLocation() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .historyInputLocation(FOO);
        assertThat(op.options().get("--historyInputLocation")).isEqualTo(FOO);
    }

    @Test
    void historyOutputLocation() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .historyOutputLocation(FOO);
        assertThat(op.options().get("--historyOutputLocation")).isEqualTo(FOO);
    }

    @Test
    void includeLaunchClasspath() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .includeLaunchClasspath(true);
        assertThat(op.options().get("--includeLaunchClasspath")).isEqualTo(TRUE);

        op = new PitestOperation()
                .fromProject(new Project())
                .includeLaunchClasspath(false);
        assertThat(op.options().get("--includeLaunchClasspath")).isEqualTo(FALSE);
    }

    @Test
    void includedGroups() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .includedGroups(FOO, BAR);
        assertThat(op.options().get("--includedGroups")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .includedGroups(List.of(FOO, BAR));
        assertThat(op.options().get("--includedGroups")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
    void includedTestMethods() {
        var op = new PitestOperation()
                .fromProject(new Project())
                .includedTestMethods(FOO);
        assertThat(op.options().get("--includedTestMethods")).isEqualTo(FOO);
    }

    @Test
    void inputEncoding() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .inputEncoding(FOO);
        assertThat(op.options().get("--inputEncoding")).isEqualTo(FOO);
    }

    @Test
    void jvmArgs() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .jvmArgs(FOO, BAR);
        assertThat(op.options().get("--jvmArgs")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .jvmArgs(List.of(FOO, BAR));
        assertThat(op.options().get("--jvmArgs")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
    void jvmPath() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .jvmPath(FOO);
        assertThat(op.options().get("--jvmPath")).isEqualTo(FOO);
    }

    @Test
    void maxMutationsPerClass() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .maxMutationsPerClass(12);
        assertThat(op.options().get("--maxMutationsPerClass")).isEqualTo("12");
    }

    @Test
    void maxSurviving() {
        var op = new PitestOperation()
                .fromProject(new Project())
                .maxSurviving(1);
        assertThat(op.options().get("--maxSurviving")).isEqualTo("1");
    }

    @Test
    void mutableCodePaths() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .mutableCodePaths(FOO, BAR);
        assertThat(op.options().get("--mutableCodePaths")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .mutableCodePaths(List.of(FOO, BAR));
        assertThat(op.options().get("--mutableCodePaths")).as(AS_LIST).isEqualTo(FOOBAR);
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

        op = new PitestOperation()
                .fromProject(new BaseProject())
                .mutationThreshold(101);
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

        op = new PitestOperation()
                .fromProject(new Project())
                .mutators(List.of(FOO, BAR));
        assertThat(op.options().get("--mutators")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
    void outputEncoding() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .outputEncoding(FOO);
        assertThat(op.options().get("--outputEncoding")).isEqualTo(FOO);
    }

    @Test
    void outputFormats() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .outputFormats(FOO, BAR);
        assertThat(op.options().get("--outputFormats")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .outputFormats(List.of(FOO, BAR));
        assertThat(op.options().get("--outputFormats")).as(AS_LIST).isEqualTo(FOOBAR);
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
        var op = new PitestOperation()
                .fromProject(new Project())
                .projectBase(FOO);
        assertThat(op.options().get("--projectBase")).isEqualTo(FOO);
    }

    @Test
    void reportDir() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .reportDir(FOO);
        assertThat(op.options().get("--reportDir")).isEqualTo(FOO);
    }

    @Test
    void skipFailingTests() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .skipFailingTests(true);
        assertThat(op.options().get("--skipFailingTests")).isEqualTo(TRUE);

        op = new PitestOperation()
                .fromProject(new Project())
                .skipFailingTests(false);
        assertThat(op.options().get("--skipFailingTests")).isEqualTo(FALSE);
    }

    @Test
    void sourceDirs() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .sourceDirs(FOO, BAR);
        assertThat(op.options().get("--sourceDirs")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .sourceDirs(List.of(FOO, BAR));
        assertThat(op.options().get("--sourceDirs")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
    void targetClasses() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .targetClasses(FOO, BAR);
        assertThat(op.options().get("--targetClasses")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .targetClasses(List.of(FOO, BAR));
        assertThat(op.options().get("--targetClasses")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
    void targetTests() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .targetTests(FOO, BAR);
        assertThat(op.options().get("--targetTests")).isEqualTo(FOOBAR);

        op = new PitestOperation()
                .fromProject(new Project())
                .targetTests(List.of(FOO, BAR));
        assertThat(op.options().get("--targetTests")).as(AS_LIST).isEqualTo(FOOBAR);
    }

    @Test
    void testStrengthThreshold() {
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

    @Test
    void timestampedReports() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .timestampedReports(true);
        assertThat(op.options().get("--timestampedReports")).isEqualTo(TRUE);

        op = new PitestOperation()
                .fromProject(new Project())
                .timestampedReports(false);
        assertThat(op.options().get("--timestampedReports")).isEqualTo(FALSE);
    }

    @Test
    void useClasspathJar() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .useClasspathJar(true);
        assertThat(op.options().get("--useClasspathJar")).isEqualTo(TRUE);

        op = new PitestOperation()
                .fromProject(new Project())
                .useClasspathJar(false);
        assertThat(op.options().get("--useClasspathJar")).isEqualTo(FALSE);
    }

    @Test
    void verbose() {
        var op = new PitestOperation()
                .fromProject(new BaseProject())
                .verbose(true);
        assertThat(op.options().get("--verbose")).isEqualTo(TRUE);

        op = new PitestOperation()
                .fromProject(new Project())
                .verbose(false);
        assertThat(op.options().get("--verbose")).isEqualTo(FALSE);
    }

    @Test
    void verbosity() {
        var op = new PitestOperation()
                .fromProject(new Project())
                .verbosity(FOO);
        assertThat(op.options().get("--verbosity")).isEqualTo(FOO);
    }
}
