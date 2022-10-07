/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.ALogListener;
import org.faktorips.devtools.abstraction.AVersion;
import org.junit.Test;

public class PlainJavaImplementationTest {

    @Test
    public void testGet() {
        assertThat(PlainJavaImplementation.get(), is(sameInstance(PlainJavaImplementation.INSTANCE)));
    }

    @Test
    public void testIsEclipse() {
        assertThat(PlainJavaImplementation.get().isEclipse(), is(false));
    }

    @Test
    public void testGetId() {
        assertThat(PlainJavaImplementation.get().getId(), is("Plain Java Faktor-IPS")); //$NON-NLS-1$
    }

    @Test
    public void testGetWorkspace_ReturnsSetWorkspace() throws IOException {
        File tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory(PlainJavaImplementationTest.class.getSimpleName()).toFile();
            PlainJavaImplementation plainJavaImplementation = PlainJavaImplementation.get();
            PlainJavaWorkspace workspace = new PlainJavaWorkspace(tmpDir);
            plainJavaImplementation.setWorkspace(workspace);

            assertThat(plainJavaImplementation.getWorkspace(), is(workspace));
        } finally {
            if (tmpDir != null) {
                FileUtils.deleteDirectory(tmpDir);
            }
        }
    }

    @Test
    public void testGetWorkspace_CreatesWorkspace() throws IOException {
        File tmpDir = null;
        try {
            PlainJavaImplementation plainJavaImplementation = PlainJavaImplementation.get();
            plainJavaImplementation.setWorkspace(null);

            PlainJavaWorkspace workspace = plainJavaImplementation.getWorkspace();

            assertThat(workspace, is(notNullValue()));
            tmpDir = workspace.getRoot().getLocation().toFile();
            assertThat(tmpDir.exists(), is(true));
            assertThat(tmpDir.getName(), containsString("Workspace")); //$NON-NLS-1$
        } finally {
            if (tmpDir != null) {
                FileUtils.deleteDirectory(tmpDir);
            }
        }
    }

    @Test
    public void testGetLocale() {
        assertThat(PlainJavaImplementation.get().getLocale(), is(Locale.getDefault()));
    }

    @Test
    public void testGetVersion() {
        assertThat(PlainJavaImplementation.get().getVersion(), is(AVersion.parse("22.12"))); //$NON-NLS-1$
    }

    @Test
    public void testLog() {
        Map<Level, String> logged = new LinkedHashMap<>();
        Logger logger = new Logger(PlainJavaImplementation.ID, null) {
            @Override
            public void log(Level level, String msg) {
                logged.put(level, msg);
                super.log(level, msg);
            }
        };
        LogManager.getLogManager().addLogger(logger);
        ALog log = PlainJavaImplementation.get().getLog();
        List<IStatus> listened = new LinkedList<>();
        ALogListener logListener = (status, $) -> listened.add(status);
        log.addLogListener(logListener);

        Status status1 = new Status(IStatus.ERROR, "foo1", "bar1"); //$NON-NLS-1$ //$NON-NLS-2$
        Status status2 = new Status(IStatus.WARNING, "foo2", "bar2"); //$NON-NLS-1$ //$NON-NLS-2$
        Status status3 = new Status(IStatus.INFO, "foo3", "bar3"); //$NON-NLS-1$ //$NON-NLS-2$
        Status status4 = new Status(IStatus.OK, "foo4", "bar4"); //$NON-NLS-1$ //$NON-NLS-2$

        log.log(status1);
        log.log(status2);
        log.removeLogListener(logListener);
        log.log(status3);
        log.log(status4);

        assertThat(listened, hasItems(status1, status2));
        assertThat(listened, not(hasItems(status3, status4)));
        assertThat(logged.get(Level.SEVERE), is("bar1")); //$NON-NLS-1$
        assertThat(logged.get(Level.WARNING), is("bar2")); //$NON-NLS-1$
        assertThat(logged.get(Level.INFO), is("bar3")); //$NON-NLS-1$
        assertThat(logged.get(Level.FINE), is("bar4")); //$NON-NLS-1$
    }

}
