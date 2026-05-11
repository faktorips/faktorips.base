/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EclipseIniUtilTest {

    private static final String PATH_TO_WORKSPACE = Platform.getLocation().toString();
    private static final String ECLIPSE_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$
    private static final String EXIT_DATA_PROPERTY = IApplicationContext.EXIT_DATA_PROPERTY;

    private String originalVmArgs;
    private String originalExitData;

    @Before
    public void saveProperties() {
        originalVmArgs = System.getProperty(ECLIPSE_VMARGS);
        originalExitData = System.getProperty(EXIT_DATA_PROPERTY);
    }

    @After
    public void restoreProperties() {
        restoreProperty(ECLIPSE_VMARGS, originalVmArgs);
        restoreProperty(EXIT_DATA_PROPERTY, originalExitData);
    }

    private static void restoreProperty(String key, String originalValue) {
        if (originalValue == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, originalValue);
        }
    }

    @Test
    public void readVmArgs_propertyNotSet_returnsEmpty() {
        System.clearProperty(ECLIPSE_VMARGS);

        assertThat(EclipseIniUtil.readVmArgs(), is(""));
    }

    @Test
    public void readVmArgs_blankProperty_returnsEmpty() {
        System.setProperty(ECLIPSE_VMARGS, "   ");

        assertThat(EclipseIniUtil.readVmArgs(), is(""));
    }

    @Test
    public void readVmArgs_singleArg_returnsArg() {
        System.setProperty(ECLIPSE_VMARGS, "-Xmx1024m");

        assertThat(EclipseIniUtil.readVmArgs(), is("\n-Xmx1024m"));
    }

    @Test
    public void readVmArgs_multipleArgs_returnsAllArgs() {
        System.setProperty(ECLIPSE_VMARGS, "-Xms256m\n-Xmx1024m\n-Dfoo=bar");

        assertThat(EclipseIniUtil.readVmArgs(), is("\n-Xms256m\n-Xmx1024m\n-Dfoo=bar"));
    }

    @Test
    public void readVmArgs_jarArgIsExcluded() {
        System.setProperty(ECLIPSE_VMARGS, "-Xmx1024m\n-jar\n/path/to/some.jar\n-Dfoo=bar");

        assertThat(EclipseIniUtil.readVmArgs(), is("\n-Xmx1024m\n-Dfoo=bar"));
    }

    @Test
    public void readVmArgs_jarArgAtEnd_isExcluded() {
        System.setProperty(ECLIPSE_VMARGS, "-Xmx1024m\n-jar\n/path/to/some.jar");

        assertThat(EclipseIniUtil.readVmArgs(), is("\n-Xmx1024m"));
    }

    @Test
    public void readVmArgs_windowsLineEndings_parsedCorrectly() {
        System.setProperty(ECLIPSE_VMARGS, "-Xms256m\r\n-Xmx1024m");

        assertThat(EclipseIniUtil.readVmArgs(), is("\n-Xms256m\n-Xmx1024m"));
    }

    @Test
    public void readVmArgs_jarArgAtEndWithoutValue_isExcluded() {
        System.setProperty(ECLIPSE_VMARGS, "-Xmx1024m\n-jar");

        assertThat(EclipseIniUtil.readVmArgs(), is("\n-Xmx1024m"));
    }

    @Test
    public void setCmdLineParams_setsExitDataWithWorkspacePath() {
        System.clearProperty(ECLIPSE_VMARGS);
        System.clearProperty(EXIT_DATA_PROPERTY);

        EclipseIniUtil.setCmdLineParams(PATH_TO_WORKSPACE);

        String exitData = System.getProperty(EXIT_DATA_PROPERTY);
        assertThat(exitData, containsString(PATH_TO_WORKSPACE));
    }

    @Test
    public void setCmdLineParams_setsSkipWorkspaceDialogProperty() {
        System.clearProperty(ECLIPSE_VMARGS);
        System.clearProperty(EXIT_DATA_PROPERTY);

        EclipseIniUtil.setCmdLineParams(PATH_TO_WORKSPACE);

        String exitData = System.getProperty(EXIT_DATA_PROPERTY);
        assertThat(exitData, containsString(IpsApplication.SKIP_WORKSPACE_DIALOG_PROPERTY));
    }

    @Test
    public void setCmdLineParams_includesVmArgs() {
        System.setProperty(ECLIPSE_VMARGS, "-Xmx512m"); //$NON-NLS-1$
        System.clearProperty(EXIT_DATA_PROPERTY);

        EclipseIniUtil.setCmdLineParams(PATH_TO_WORKSPACE);

        String exitData = System.getProperty(EXIT_DATA_PROPERTY);
        assertThat(exitData, containsString("-Xmx512m")); //$NON-NLS-1$
    }

    @Test
    public void setCmdLineParams_excludesJarFromVmArgs() {
        System.setProperty(ECLIPSE_VMARGS, "-Xmx512m\n-jar\n/eclipse/plugins/startup.jar\n-Dfoo=bar"); //$NON-NLS-1$
        System.clearProperty(EXIT_DATA_PROPERTY);

        EclipseIniUtil.setCmdLineParams(PATH_TO_WORKSPACE);

        String exitData = System.getProperty(EXIT_DATA_PROPERTY);
        assertThat(exitData, containsString("-Xmx512m")); //$NON-NLS-1$
        assertThat(exitData, containsString("-Dfoo=bar")); //$NON-NLS-1$
        assertThat(exitData, not(containsString("/eclipse/plugins/startup.jar"))); //$NON-NLS-1$
    }

    @Test
    public void setCmdLineParams_existingExitDataIsPreserved() {
        System.clearProperty(ECLIPSE_VMARGS);
        System.setProperty(EXIT_DATA_PROPERTY, "existing"); //$NON-NLS-1$

        EclipseIniUtil.setCmdLineParams(PATH_TO_WORKSPACE);

        String exitData = System.getProperty(EXIT_DATA_PROPERTY);
        assertThat(exitData, startsWith("existing")); //$NON-NLS-1$
    }
}
