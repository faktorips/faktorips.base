/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.mojo.internal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BuildLogPrintStreamTest {

    private BuildLogPrintStream stream;

    @BeforeEach
    public void setup() throws IOException {
        stream = new BuildLogPrintStream("testLogger");
    }

    @AfterEach
    public void tearDown() {
        stream.cleanUp();
    }

    @Test
    public void testFilteringAntLoggingWithException() throws IOException {

        String toFilter = IOUtils.toString(new FileInputStream(
                new File("./src/test/resources/buildOutputException.txt")), Charset.forName("UTF-8"));

        String filtered = stream.filter(toFilter);

        assertThat(filtered, is(sizeLessThan(toFilter)));
        assertThat(filtered, not(containsString(
                "java.io.FileNotFoundException: org.eclipse.equinox.simpleconfigurator/bundles.info")));
        assertThat(filtered, not(containsString(
                "at org.apache.tools.ant.helper.DefaultExecutor.executeTargets(DefaultExecutor.java:41)")));
        stream.cleanUp();
    }

    @Test
    public void testFilteringAntLoggingWithOutException() throws IOException {

        String toFilter = IOUtils.toString(new FileInputStream(
                new File("./src/test/resources/buildOutput.txt")), Charset.forName("UTF-8"));

        String filtered = stream.filter(toFilter);

        assertThat(filtered, is(sizeEqualTo(toFilter)));
        assertThat(filtered, is(toFilter));
    }

    public static Matcher<String> sizeLessThan(String text) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("the expected sting length is not smaller");
            }

            @Override
            protected boolean matchesSafely(String otherText) {
                return otherText.length() < text.length();
            }
        };
    }

    public static Matcher<String> sizeEqualTo(String text) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("the expected sting length is not equal");
            }

            @Override
            protected boolean matchesSafely(String otherText) {
                return otherText.length() == text.length();
            }
        };
    }
}
