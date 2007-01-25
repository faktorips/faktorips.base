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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.faktorips.devtools.core.model.IIpsLoggingFrameworkConnector;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the <code>IIpsLoggingFrameworkConnector</code> interface that connects
 * to the java.util.logging logging framework. 
 * 
 * @author Peter Erzberger
 */
public class JavaUtilLoggingFrameworkConnector implements IIpsLoggingFrameworkConnector {

    private String id = "";
    
    private String getLevelExp(int level){
        
        if(level == IIpsLoggingFrameworkConnector.LEVEL_INFO){
            return "Level.INFO";
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_WARNING){
            return "Level.WARNING";
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_ERROR){
            return "Level.SEVERE";
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_DEBUG){
            return "Level.FINE";
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_TRACE){
            return "Level.FINEST";
        }
        throw new IllegalArgumentException("The specified logging level is not defined: " + level);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getLogConditionExp(int level, String loggerExpression, List usedClasses) {
        usedClasses.add(Level.class.getName());
        return loggerExpression + ".isLoggable(" + getLevelExp(level) + ")";
    }

    private String getLevelMethodName(int level){
        if(level == IIpsLoggingFrameworkConnector.LEVEL_INFO){
            return "info";
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_WARNING){
            return "warning";
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_ERROR){
            return "severe";
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_DEBUG){
            return "fine";
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_TRACE){
            return "finest";
        }
        throw new IllegalArgumentException("The specified logging level is not defined: " + level);
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForMessage(int level, String msgConstant, String loggerInstanceExp, List usedClasses) {
        StringBuffer buf = new StringBuffer();
        buf.append(loggerInstanceExp);
        buf.append(".");
        buf.append(getLevelMethodName(level));
        buf.append("(\"");
        buf.append(msgConstant);
        buf.append("\")");
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForMessageExp(int level, String msgExp, String loggerInstanceExp, List usedClasses) {
        StringBuffer buf = new StringBuffer();
        buf.append(loggerInstanceExp);
        buf.append(".");
        buf.append(getLevelMethodName(level));
        buf.append("(");
        buf.append(msgExp);
        buf.append(")");
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForThrowable(int level, String msgExp,
            String throwableExp,
            String loggerInstanceExp,
            List usedClasses) {
        usedClasses.add(Level.class.getName());
        StringBuffer buf = new StringBuffer();
        buf.append(loggerInstanceExp);
        buf.append(".log(");
        buf.append(getLevelExp(level));
        buf.append(", ");
        buf.append(msgExp);
        buf.append(", ");
        buf.append(throwableExp);
        buf.append(")");
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getLoggerClassName() {
        return Logger.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getLoggerInstanceStmt(String scopeExp, List usedClasses) {
        usedClasses.add(Logger.class.getName());
        StringBuffer buf = new StringBuffer();
        buf.append("Logger.getLogger(");
        buf.append(scopeExp);
        buf.append(")");
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public void setId(String id) {
        ArgumentCheck.notNull(id);
        this.id = id;
    }
}
