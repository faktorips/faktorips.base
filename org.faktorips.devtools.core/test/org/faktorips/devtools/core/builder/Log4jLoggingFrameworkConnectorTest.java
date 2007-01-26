/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.faktorips.devtools.core.model.IIpsLoggingFrameworkConnector;

public class Log4jLoggingFrameworkConnectorTest extends TestCase {

    private Log4jLoggingFrameworkConnector connector;
    private List usedClasses;
    private Logger LOGGER;
    
    public void setUp(){
        LOGGER = Logger.getLogger(Log4jLoggingFrameworkConnector.class);
        LOGGER.setLevel(Level.DEBUG);
        ConsoleAppender appender = new ConsoleAppender(new SimpleLayout());
        appender.setName("Log4jLoggingFrameworkConnectorTest Appender.");
        LOGGER.addAppender(appender);
        usedClasses = new ArrayList();
        connector = new Log4jLoggingFrameworkConnector();
        
    }
    public final void testGetLogConditionExp() {
         String exp = connector.getLogConditionExp(IIpsLoggingFrameworkConnector.LEVEL_DEBUG, "LOGGER", usedClasses);
         LOGGER.isDebugEnabled();
         assertEquals("LOGGER.isDebugEnabled()", exp);
         assertEquals(0, usedClasses.size());
         
         connector.getLogConditionExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR, "LOGGER", usedClasses);
         assertEquals(1, usedClasses.size());
    }

    public final void testGetLogStmtForMessage() {
        String exp = connector.getLogStmtForMessage(IIpsLoggingFrameworkConnector.LEVEL_ERROR,  "This is a message.", "LOGGER", usedClasses);
        LOGGER.error("This is a message.");
        assertEquals("LOGGER.error(\"This is a message.\")", exp);
        assertEquals(0, usedClasses.size());
    }

    private String getMessage(){
        return "This is a logging message.";
    }
    
    public final void testGetLogStmtForMessageExp() {
        String exp = connector.getLogStmtForMessageExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR,  "getMessage()", "LOGGER", usedClasses);
        LOGGER.error(getMessage());
        assertEquals("LOGGER.error(getMessage())", exp);
        assertEquals(0, usedClasses.size());
    }

    public final void testGetLogStmtForThrowable() {
        String exp = connector.getLogStmtForThrowable(IIpsLoggingFrameworkConnector.LEVEL_ERROR,  "getMessage()", "exception", "LOGGER", usedClasses);
        Exception exception = null;
        LOGGER.error(getMessage(), exception);
        assertEquals("LOGGER.error(getMessage(), exception)", exp);
        assertEquals(0, usedClasses.size());
    }

    public final void testGetLoggerClassName() {
        assertEquals(Logger.class.getName(), connector.getLoggerClassName());
    }

    public final void testGetLoggerInstanceStmt() {
        String exp = connector.getLoggerInstanceStmt("\"org.faktorips\"", usedClasses);
        LOGGER = Logger.getLogger("org.faktorips");
        assertEquals("Logger.getLogger(\"org.faktorips\")", exp);
        assertEquals(1, usedClasses.size());
    }

}
