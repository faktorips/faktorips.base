/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.mojo;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.faktorips.maven.plugin.mojo.internal.M2eIgnorePlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class IpsBuildMojoM2eTest {

    @TempDir
    File tempDir;

    private IpsBuildMojo mojo;

    @BeforeEach
    public void setUp() throws Exception {
        mojo = new IpsBuildMojo();
        setField("work", tempDir);
    }

    // --- resolveM2eIgnorePlugins ---

    @Test
    public void resolveM2eIgnorePlugins_defaultsToSpotbugs() throws Exception {
        List<M2eIgnorePlugin> result = invokeResolve();

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getGroupId(), is("com.github.spotbugs"));
        assertThat(result.get(0).getArtifactId(), is("spotbugs-maven-plugin"));
        assertThat(result.get(0).getGoals(), is("spotbugs,check"));
    }

    @Test
    public void resolveM2eIgnorePlugins_emptyListFallsBackToDefault() throws Exception {
        setField("m2eIgnorePlugins", List.of());
        List<M2eIgnorePlugin> result = invokeResolve();

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getGroupId(), is("com.github.spotbugs"));
    }

    @Test
    public void resolveM2eIgnorePlugins_userListReplaceDefault() throws Exception {
        M2eIgnorePlugin custom = plugin("com.example", "my-plugin", "[1,)", "generate");
        setField("m2eIgnorePlugins", List.of(custom));

        List<M2eIgnorePlugin> result = invokeResolve();

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getGroupId(), is("com.example"));
        assertThat(result.get(0).getArtifactId(), is("my-plugin"));
        assertThat(result.get(0).getVersionRange(), is("[1,)"));
        assertThat(result.get(0).getGoals(), is("generate"));
    }

    @Test
    public void resolveM2eIgnorePlugins_invalidEntry_throwsMojoExecutionException() throws Exception {
        M2eIgnorePlugin invalid = new M2eIgnorePlugin();
        invalid.setGroupId("com.example");
        // artifactId missing
        invalid.setGoals("generate");
        setField("m2eIgnorePlugins", List.of(invalid));

        assertThrows(MojoExecutionException.class, this::invokeResolve);
    }

    @Test
    public void resolveM2eIgnorePlugins_missingGoals_throwsMojoExecutionException() throws Exception {
        M2eIgnorePlugin invalid = new M2eIgnorePlugin();
        invalid.setGroupId("com.example");
        invalid.setArtifactId("my-plugin");
        // goals missing
        setField("m2eIgnorePlugins", List.of(invalid));

        MojoExecutionException ex = assertThrows(MojoExecutionException.class, this::invokeResolve);
        assertThat(ex.getMessage(), containsString("goals"));
    }

    // --- writeM2eLifecycleMappingOverrides ---

    @Test
    public void writeM2eLifecycleMappingOverrides_defaultWritesSpotbugsEntry() throws Exception {
        invokeWrite();

        String content = readMappingFile();
        assertThat(content, containsString("com.github.spotbugs"));
        assertThat(content, containsString("spotbugs-maven-plugin"));
        assertThat(content, containsString("<goal>spotbugs</goal>"));
        assertThat(content, containsString("<goal>check</goal>"));
        assertThat(content, containsString("<action><ignore/></action>"));
    }

    @Test
    public void writeM2eLifecycleMappingOverrides_customPlugin_writesCorrectEntry() throws Exception {
        setField("m2eIgnorePlugins", List.of(plugin("com.example", "my-plugin", "[1,)", "generate")));

        invokeWrite();

        String content = readMappingFile();
        assertThat(content, containsString("com.example"));
        assertThat(content, containsString("my-plugin"));
        assertThat(content, containsString("<goal>generate</goal>"));
    }

    @Test
    public void writeM2eLifecycleMappingOverrides_multipleGoals_eachOnOwnLine() throws Exception {
        setField("m2eIgnorePlugins", List.of(plugin("com.example", "my-plugin", "[1,)", "spotbugs, check")));

        invokeWrite();

        String content = readMappingFile();
        assertThat(content, containsString("<goal>spotbugs</goal>"));
        assertThat(content, containsString("<goal>check</goal>"));
    }

    @Test
    public void writeM2eLifecycleMappingOverrides_emptyList_writesSpotbugsDefault() throws Exception {
        setField("m2eIgnorePlugins", List.of());

        invokeWrite();

        String content = readMappingFile();
        assertThat(content, containsString("com.github.spotbugs"));
    }

    @Test
    public void writeM2eLifecycleMappingOverrides_twoPlugins_bothPresent() throws Exception {
        setField("m2eIgnorePlugins", List.of(
                plugin("com.github.spotbugs", "spotbugs-maven-plugin", "[4,)", "spotbugs,check"),
                plugin("com.example", "my-plugin", "[1,)", "generate")));

        invokeWrite();

        String content = readMappingFile();
        assertThat(content, containsString("com.github.spotbugs"));
        assertThat(content, containsString("com.example"));
    }

    @Test
    public void writeM2eLifecycleMappingOverrides_ioError_throwsMojoExecutionException() throws Exception {
        // Make the m2e metadata directory a read-only file so writing into it fails.
        File m2eParentDir = new File(tempDir, "data/.metadata/.plugins");
        m2eParentDir.mkdirs();
        File blocker = new File(m2eParentDir, "org.eclipse.m2e.core");
        blocker.createNewFile();
        // Now the path that would be a directory is a regular file — forceMkdir will fail.
        assertThrows(MojoExecutionException.class, this::invokeWrite);
    }

    // --- helpers ---

    private M2eIgnorePlugin plugin(String groupId, String artifactId, String versionRange, String goals) {
        M2eIgnorePlugin p = new M2eIgnorePlugin();
        p.setGroupId(groupId);
        p.setArtifactId(artifactId);
        p.setVersionRange(versionRange);
        p.setGoals(goals);
        return p;
    }

    @SuppressWarnings("unchecked")
    private List<M2eIgnorePlugin> invokeResolve() throws Exception {
        Method m = IpsBuildMojo.class.getDeclaredMethod("resolveM2eIgnorePlugins");
        m.setAccessible(true);
        try {
            return (List<M2eIgnorePlugin>)m.invoke(mojo);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MojoExecutionException mee) {
                throw mee;
            }
            throw e;
        }
    }

    private void invokeWrite() throws Exception {
        Method m = IpsBuildMojo.class.getDeclaredMethod("writeM2eLifecycleMappingOverrides");
        m.setAccessible(true);
        try {
            m.invoke(mojo);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MojoExecutionException mee) {
                throw mee;
            }
            throw e;
        }
    }

    private String readMappingFile() throws Exception {
        File mappingFile = new File(tempDir,
                "data/.metadata/.plugins/org.eclipse.m2e.core/lifecycle-mapping-metadata.xml");
        return Files.readString(mappingFile.toPath());
    }

    private void setField(String name, Object value) throws Exception {
        Field f = IpsBuildMojo.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(mojo, value);
    }
}
