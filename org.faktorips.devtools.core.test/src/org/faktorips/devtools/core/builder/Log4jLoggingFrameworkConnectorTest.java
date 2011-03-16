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
import java.util.List;

import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.junit.Before;
import org.junit.Test;

public class Log4jLoggingFrameworkConnectorTest {

    private Log4jLoggingFrameworkConnector connector;
    private List<String> usedClasses;

    @Before
    public void setUp() {
        usedClasses = new ArrayList<String>();
        connector = new Log4jLoggingFrameworkConnector();

    }

    @Test
    public void testGetLogConditionExp() {
        String exp = connector.getLogConditionExp(IIpsLoggingFrameworkConnector.LEVEL_DEBUG, "LOGGER", usedClasses); //$NON-NLS-1$
        assertEquals("LOGGER.isDebugEnabled()", exp); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());

        connector.getLogConditionExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR, "LOGGER", usedClasses); //$NON-NLS-1$
        assertEquals(1, usedClasses.size());
    }

    @Test
    public void testGetLogStmtForMessage() {
        String exp = connector.getLogStmtForMessage(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "This is a message.", "LOGGER", usedClasses); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("LOGGER.error(\"This is a message.\")", exp); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());
    }

    @Test
    public void testGetLogStmtForMessageExp() {
        String exp = connector.getLogStmtForMessageExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "getMessage()", "LOGGER", usedClasses); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("LOGGER.error(getMessage())", exp); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());
    }

    @Test
    public void testGetLogStmtForThrowable() {
        String exp = connector.getLogStmtForThrowable(IIpsLoggingFrameworkConnector.LEVEL_ERROR,
                "getMessage()", "exception", "LOGGER", usedClasses); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("LOGGER.error(getMessage(), exception)", exp); //$NON-NLS-1$
        assertEquals(0, usedClasses.size());
    }

    @Test
    public void testGetLoggerClassName() {
        assertEquals("org.apache.log4j.Logger", connector.getLoggerClassName());
    }

    @Test
    public void testGetLoggerInstanceStmt() {
        String exp = connector.getLoggerInstanceStmt("\"org.faktorips\"", usedClasses); //$NON-NLS-1$
        assertEquals("Logger.getLogger(\"org.faktorips\")", exp); //$NON-NLS-1$
        assertEquals(1, usedClasses.size());
    }

}
