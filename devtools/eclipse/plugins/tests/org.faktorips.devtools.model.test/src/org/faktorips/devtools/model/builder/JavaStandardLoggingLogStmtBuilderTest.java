/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.junit.Before;
import org.junit.Test;

@Deprecated(since = "21.12")
public class JavaStandardLoggingLogStmtBuilderTest {

    private JavaUtilLoggingFrameworkConnector loggingFrameworkConnector;

    @Before
    public void setUp() {
        loggingFrameworkConnector = new JavaUtilLoggingFrameworkConnector();
    }

    @Test
    public void testGetLogConditionExp() {
        ArrayList<String> usedClasses = new ArrayList<>();
        String loggerExp = loggingFrameworkConnector.getLoggerInstanceStmt("\"com.foo\"", usedClasses); //$NON-NLS-1$
        String stmt = loggingFrameworkConnector.getLogConditionExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                loggerExp, usedClasses);
        assertEquals("Logger.getLogger(\"com.foo\").isLoggable(Level.SEVERE)", stmt); //$NON-NLS-1$

        // test against the java compiler of the real code
        Logger.getLogger("com.foo").isLoggable(Level.SEVERE); //$NON-NLS-1$

        assertEquals(2, usedClasses.size());
        assertEquals(Logger.class.getName(), usedClasses.get(0));
        assertEquals(Level.class.getName(), usedClasses.get(1));
    }

    @Test
    public void testGetLogStmtForMessage() {
        String loggerExp = "LOGGER"; //$NON-NLS-1$
        ArrayList<String> usedClasses = new ArrayList<>();
        String stmt = loggingFrameworkConnector.getLogStmtForMessage(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "Message", loggerExp, usedClasses); //$NON-NLS-1$
        assertEquals("LOGGER.severe(\"Message\")", stmt); //$NON-NLS-1$

        // test against the java compiler of the real code
        Logger LOGGER = Logger.getLogger("com.foo"); //$NON-NLS-1$
        LOGGER.severe("Message"); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());
    }

    private String getMessage() {
        return "Message"; //$NON-NLS-1$
    }

    @Test
    public void testGetLogStmtForMsgExp() {
        String loggerExp = "LOGGER"; //$NON-NLS-1$
        ArrayList<String> usedClasses = new ArrayList<>();
        String stmt = loggingFrameworkConnector.getLogStmtForMessageExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "getMessage()", loggerExp, usedClasses); //$NON-NLS-1$
        assertEquals("LOGGER.severe(getMessage())", stmt); //$NON-NLS-1$

        // test against the java compiler of the real code
        Logger LOGGER = Logger.getLogger("com.foo"); //$NON-NLS-1$
        LOGGER.severe(getMessage());
        assertEquals(0, usedClasses.size());
    }

    @Test
    public void testGetLogStmtForThrowable() {
        String loggerExp = "LOGGER"; //$NON-NLS-1$
        ArrayList<String> usedClasses = new ArrayList<>();
        String stmt = loggingFrameworkConnector.getLogStmtForThrowable(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "\"Message\"", "exception", loggerExp, usedClasses); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("LOGGER.log(Level.SEVERE, \"Message\", exception)", stmt); //$NON-NLS-1$

        // test against the java compiler of the real code
        Logger LOGGER = Logger.getLogger("com.foo"); //$NON-NLS-1$
        Exception exception = null;
        LOGGER.log(Level.SEVERE, "Message", exception); //$NON-NLS-1$
        assertEquals(1, usedClasses.size());
        assertEquals(Level.class.getName(), usedClasses.get(0));
    }

    @Test
    public void testGetLoggerClassName() {
        String loggerClass = loggingFrameworkConnector.getLoggerClassName();
        assertEquals(Logger.class.getName(), loggerClass);
    }

    @Test
    public void testGetLoggerInstanceStmtStringStringList() {
        ArrayList<String> usedClasses = new ArrayList<>();
        String stmt = loggingFrameworkConnector.getLoggerInstanceStmt("\"com.foo\"", usedClasses); //$NON-NLS-1$
        assertEquals("Logger.getLogger(\"com.foo\")", stmt); //$NON-NLS-1$
        assertEquals(1, usedClasses.size());
        assertEquals(Logger.class.getName(), usedClasses.get(0));

        // test against the java compiler of the real code
        Logger.getLogger("com.foo"); //$NON-NLS-1$
    }
}
