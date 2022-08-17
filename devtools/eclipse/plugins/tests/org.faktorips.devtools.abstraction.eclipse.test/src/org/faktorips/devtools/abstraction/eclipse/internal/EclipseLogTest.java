/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.ALogListener;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EclipseLogTest extends EclipseAbstractionTestSetup {

    private final Map<String, IStatus> loggerImpl = new HashMap<>();
    private final ALogListener logListener = (status, contextId) -> loggerImpl.put(contextId, status);

    private ALog logger;

    @Before
    public void setUp() {
        logger = Abstractions.getLog();
        logger.addLogListener(logListener);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        logger.removeLogListener(logListener);
        loggerImpl.clear();
    }

    @Test
    public void testAEclipseLog() {
        assertThat(logger.unwrap(), is(instanceOf(ILog.class)));
    }

    @Test
    public void testLog() {
        String pluginId = "AEclipseLogTest.id";
        String message = "there is an error";

        logger.log(new Status(IStatus.ERROR, pluginId, message));

        assertThat(loggerImpl.isEmpty(), is(false));
        IStatus status = loggerImpl.get("org.faktorips.devtools.abstraction.eclipse");
        assertThat(status.getSeverity(), is(IStatus.ERROR));
        assertThat(status.getPlugin(), is(pluginId));
        assertThat(status.getMessage(), is(message));
    }
}
