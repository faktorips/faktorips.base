/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.junit.Before;
import org.junit.Test;

public class JavaStandardLoggingLogStmtBuilderTest {

    private JavaUtilLoggingFrameworkConnector loggingFrameworkConnector;

    @Before
    public void setUp() {
        loggingFrameworkConnector = new JavaUtilLoggingFrameworkConnector();
    }

    @Test
    public void testGetLogConditionExp() {
        ArrayList<String> usedClasses = new ArrayList<String>();
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
        ArrayList<String> usedClasses = new ArrayList<String>();
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
        ArrayList<String> usedClasses = new ArrayList<String>();
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
        ArrayList<String> usedClasses = new ArrayList<String>();
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
        ArrayList<String> usedClasses = new ArrayList<String>();
        String stmt = loggingFrameworkConnector.getLoggerInstanceStmt("\"com.foo\"", usedClasses); //$NON-NLS-1$
        assertEquals("Logger.getLogger(\"com.foo\")", stmt); //$NON-NLS-1$
        assertEquals(1, usedClasses.size());
        assertEquals(Logger.class.getName(), usedClasses.get(0));

        // test against the java compiler of the real code
        Logger.getLogger("com.foo"); //$NON-NLS-1$
    }
}
