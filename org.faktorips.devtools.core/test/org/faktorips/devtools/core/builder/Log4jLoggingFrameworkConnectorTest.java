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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;

public class Log4jLoggingFrameworkConnectorTest extends TestCase {

    private Log4jLoggingFrameworkConnector connector;
    private List<String> usedClasses;
    private Logger LOGGER;

    @Override
    public void setUp() {
        LOGGER = Logger.getLogger(Log4jLoggingFrameworkConnector.class);
        LOGGER.setLevel(Level.DEBUG);
        ConsoleAppender appender = new ConsoleAppender(new SimpleLayout());
        appender.setName("Log4jLoggingFrameworkConnectorTest Appender."); //$NON-NLS-1$
        LOGGER.addAppender(appender);
        usedClasses = new ArrayList<String>();
        connector = new Log4jLoggingFrameworkConnector();

    }

    public final void testGetLogConditionExp() {
        String exp = connector.getLogConditionExp(IIpsLoggingFrameworkConnector.LEVEL_DEBUG, "LOGGER", usedClasses); //$NON-NLS-1$
        LOGGER.isDebugEnabled();
        assertEquals("LOGGER.isDebugEnabled()", exp); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());

        connector.getLogConditionExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR, "LOGGER", usedClasses); //$NON-NLS-1$
        assertEquals(1, usedClasses.size());
    }

    public final void testGetLogStmtForMessage() {
        String exp = connector.getLogStmtForMessage(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "This is a message.", "LOGGER", usedClasses); //$NON-NLS-1$ //$NON-NLS-2$
        LOGGER.error("This is a message."); //$NON-NLS-1$
        assertEquals("LOGGER.error(\"This is a message.\")", exp); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());
    }

    private String getMessage() {
        return "This is a logging message."; //$NON-NLS-1$
    }

    public final void testGetLogStmtForMessageExp() {
        String exp = connector.getLogStmtForMessageExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "getMessage()", "LOGGER", usedClasses); //$NON-NLS-1$ //$NON-NLS-2$
        LOGGER.error(getMessage());
        assertEquals("LOGGER.error(getMessage())", exp); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());
    }

    public final void testGetLogStmtForThrowable() {
        String exp = connector.getLogStmtForThrowable(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "getMessage()", "exception", "LOGGER", usedClasses); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Exception exception = null;
        LOGGER.error(getMessage(), exception);
        assertEquals("LOGGER.error(getMessage(), exception)", exp); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());
    }

    public final void testGetLoggerClassName() {
        assertEquals(Logger.class.getName(), connector.getLoggerClassName());
    }

    public final void testGetLoggerInstanceStmt() {
        String exp = connector.getLoggerInstanceStmt("\"org.faktorips\"", usedClasses); //$NON-NLS-1$
        LOGGER = Logger.getLogger("org.faktorips"); //$NON-NLS-1$
        assertEquals("Logger.getLogger(\"org.faktorips\")", exp); //$NON-NLS-1$
        assertEquals(1, usedClasses.size());
    }

}
