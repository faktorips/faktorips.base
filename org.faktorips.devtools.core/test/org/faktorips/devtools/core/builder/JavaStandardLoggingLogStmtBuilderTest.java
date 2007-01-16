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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.faktorips.devtools.core.model.IIpsLoggingFrameworkConnector;

import junit.framework.TestCase;

public class JavaStandardLoggingLogStmtBuilderTest extends TestCase {

    private JavaUtilLoggingFrameworkConnector loggingFrameworkConnector;
    
    public void setUp(){
        loggingFrameworkConnector = new JavaUtilLoggingFrameworkConnector();
    }
    
    public final void testGetLogConditionExp() {
        ArrayList usedClasses = new ArrayList();
        String loggerExp = loggingFrameworkConnector.getLoggerInstanceStmt("\"com.foo\"", usedClasses);
        String stmt = loggingFrameworkConnector.getLogConditionExp(IIpsLoggingFrameworkConnector.LEVEL_ERROR, loggerExp, usedClasses);
        assertEquals("Logger.getLogger(\"com.foo\").isLoggable(Level.SEVERE)", stmt);
        
        //test against the java compiler of the real code
        Logger.getLogger("com.foo").isLoggable(Level.SEVERE);
        
        assertEquals(2, usedClasses.size());
        assertEquals(Logger.class.getName(), usedClasses.get(0));
        assertEquals(Level.class.getName(), usedClasses.get(1));
    }

    public final void testGetLogStmtForMessage() {
        String loggerExp = "LOGGER";
        ArrayList usedClasses = new ArrayList();
        String stmt = loggingFrameworkConnector.getLogStmtForMessage(
                IIpsLoggingFrameworkConnector.LEVEL_ERROR, "Message", loggerExp, usedClasses);
        assertEquals("LOGGER.severe(\"Message\")", stmt);

//      test against the java compiler of the real code
        Logger LOGGER = Logger.getLogger("com.foo");
        LOGGER.severe("Message");
        assertEquals(0, usedClasses.size());
    }

    private String getMessage(){
        return "Message";
    }
    
    public final void testGetLogStmtForMsgExp() {
        String loggerExp = "LOGGER";
        ArrayList usedClasses = new ArrayList();
        String stmt = loggingFrameworkConnector.getLogStmtForMessageExp(
                IIpsLoggingFrameworkConnector.LEVEL_ERROR, "getMessage()", loggerExp, usedClasses);
        assertEquals("LOGGER.severe(getMessage())", stmt);

//      test against the java compiler of the real code
        Logger LOGGER = Logger.getLogger("com.foo");
        LOGGER.severe(getMessage());
        assertEquals(0, usedClasses.size());
    }

    public final void testGetLogStmtForThrowable() {
        String loggerExp = "LOGGER";
        ArrayList usedClasses = new ArrayList();
        String stmt = loggingFrameworkConnector.getLogStmtForThrowable(
                IIpsLoggingFrameworkConnector.LEVEL_ERROR, "\"Message\"", "exception", loggerExp, usedClasses);
        assertEquals("LOGGER.log(Level.SEVERE, \"Message\", exception)", stmt);

//      test against the java compiler of the real code
        Logger LOGGER = Logger.getLogger("com.foo");
        Exception exception = null;
        LOGGER.log(Level.SEVERE, "Message", exception);
        assertEquals(1, usedClasses.size());
        assertEquals(Level.class.getName(), usedClasses.get(0));
    }

    public final void testGetLoggerClassName() {
        String loggerClass = loggingFrameworkConnector.getLoggerClassName();
        assertEquals(Logger.class.getName(), loggerClass);
    }

    public final void testGetLoggerInstanceStmtStringStringList() {
        ArrayList usedClasses = new ArrayList();
        String stmt = loggingFrameworkConnector.getLoggerInstanceStmt("\"com.foo\"", usedClasses);
        assertEquals("Logger.getLogger(\"com.foo\")", stmt);
        assertEquals(1, usedClasses.size());
        assertEquals(Logger.class.getName(), usedClasses.get(0));
        
//      test against the java compiler of the real code
        Logger.getLogger("com.foo");
    }
}
