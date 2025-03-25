/*
 * Copyright 2023-2025 the original author or authors.
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

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;

import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;
import static rife.bld.operations.JavadocOptions.DocLinkOption.NO_MISSING;

public class PitestOperationBuild extends Project {
    final PmdOperation pmdOp = new PmdOperation()
            .fromProject(this)
            .failOnViolation(true)
            .ruleSets("config/pmd.xml");

    public PitestOperationBuild() {
        pkg = "rife.bld.extension";
        name = "PitestExtension";
        version = version(1, 0, 10, "SNAPSHOT");

        javaRelease = 17;

        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_LOCAL, MAVEN_CENTRAL, RIFE2_RELEASES, RIFE2_SNAPSHOTS);

        var pitest = version(1, 19, 0);
        scope(compile)
                .include(dependency("com.uwyn.rife2", "bld", version(2, 2, 1)));
        scope(test)
                .include(dependency("org.pitest", "pitest", pitest))
                .include(dependency("org.pitest", "pitest-command-line", pitest))
                .include(dependency("org.pitest", "pitest-junit5-plugin", version(1, 2, 2)))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 12, 1)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 12, 1)))
                .include(dependency("org.assertj", "assertj-core", version(3, 27, 3)));

        javadocOperation()
                .javadocOptions()
                .author()
                .docLint(NO_MISSING)
                .link("https://rife2.github.io/bld/")
                .link("https://rife2.github.io/rife2/");

        publishOperation()
                .repository(version.isSnapshot() ? repository("rife2-snapshot") : repository("rife2"))
                .repository(repository("github"))
                .info()
                .groupId("com.uwyn.rife2")
                .artifactId("bld-pitest")
                .description("PIT Mutation Testing Extension for bld")
                .url("https://github.com/rife2/bld-pitest")
                .developer(new PublishDeveloper()
                        .id("ethauvin")
                        .name("Erik C. Thauvin")
                        .email("erik@thauvin.net")
                        .url("https://erik.thauvin.net/")
                )
                .license(new PublishLicense()
                        .name("The Apache License, Version 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.txt")
                )
                .scm(new PublishScm()
                        .connection("scm:git:https://github.com/rife2/bld-pitest.git")
                        .developerConnection("scm:git:git@github.com:rife2/bld-pitest.git")
                        .url("https://github.com/rife2/bld-pitest")
                )
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase"));
    }

    public static void main(String[] args) {
        new PitestOperationBuild().start(args);
    }

    @BuildCommand(summary = "Runs PMD analysis")
    public void pmd() throws Exception {
        pmdOp.execute();
    }

    @BuildCommand(value = "pmd-cli", summary = "Runs PMD analysis (CLI)")
    public void pmdCli() throws Exception {
        pmdOp.includeLineNumber(false).execute();
    }

    @Override
    public void test() throws Exception {
        var os = System.getProperty("os.name");
        if (os != null && os.toLowerCase().contains("linux")) {
            new ExecOperation()
                    .fromProject(this)
                    .command("scripts/cliargs.sh")
                    .execute();
        }
        super.test();
    }
}
