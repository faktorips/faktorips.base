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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.faktorips.devtools.core.model.IIpsLoggingFrameworkConnector;

/**
 * 
 * @author Peter Erzberger
 */
public class Log4jLoggingFrameworkConnector implements IIpsLoggingFrameworkConnector {

    private String id;
    
    
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
        this.id = id;
    }

    private String build(int level, String loggerInstanceExp, String message, Builder builder){
        StringBuffer buf = new StringBuffer();
        buf.append(loggerInstanceExp);
        
        if(IIpsLoggingFrameworkConnector.LEVEL_TRACE == level){
            buf.append(builder.buildTrace(message));
        }
        else if(IIpsLoggingFrameworkConnector.LEVEL_DEBUG == level){
            buf.append(builder.buildDebug(message));
        }
        else if(IIpsLoggingFrameworkConnector.LEVEL_INFO == level){
            buf.append(builder.buildInfo(message));
        }
        else if(IIpsLoggingFrameworkConnector.LEVEL_WARNING == level){
            buf.append(builder.buildWarning(message));
        }
        else if(IIpsLoggingFrameworkConnector.LEVEL_ERROR == level){
            buf.append(builder.buildError(message));
        }
        else{
            throw new IllegalArgumentException("The value of the parameter level is not valid. " +
                    "Use the level constants of " + IIpsLoggingFrameworkConnector.class + ".");
        }
        return buf.toString();

    }
    /**
     * {@inheritDoc}
     */
    public String getLogConditionExp(int level, String loggerInstanceExp, List usedClasses) {
        return build(level, loggerInstanceExp, null, new LogConditionExpBuilder(usedClasses));
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForMessage(int level, String msgConstant, String loggerInstanceExp, List usedClasses) {
        return build(level, loggerInstanceExp, msgConstant, new LogStmtForMessageBuilder());
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForMessageExp(int level, String msgExp, String loggerInstanceExp, List usedClasses) {
        return build(level, loggerInstanceExp, msgExp, new LogStmtForMessageExpBuilder());
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForThrowable(int level,
            String msgExp,
            String throwableExp,
            String loggerInstanceExp,
            List usedClasses) {
        return build(level, loggerInstanceExp, msgExp, new LogStmtForThrowableBuilder(throwableExp));
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
        return "Logger.getLogger(" + scopeExp + ")";
    }


    private static interface Builder{
        
        public String buildInfo(String message);
        
        public String buildTrace(String message);
        
        public String buildDebug(String message);
        
        public String buildWarning(String message);
        
        public String buildError(String message);
    }
    
    private static class LogConditionExpBuilder implements Builder{

        private List usedClasses;
        
        private LogConditionExpBuilder(List usedClasses){
            this.usedClasses = usedClasses;
        }
        
        public String buildDebug(String message) {
            return ".isDebugEnabled()";
        }

        public String buildError(String message) {
            usedClasses.add(Level.class.getName());
            return ".isEnabledFor(Level.ERROR)";
        }

        public String buildInfo(String message) {
            return ".isInfoEnabled()";
        }

        public String buildTrace(String message) {
            return ".isTraceEnabled()";
        }

        public String buildWarning(String message) {
            usedClasses.add(Level.class.getName());
            return ".isEnabledFor(Level.WARN)";
        }
    }
    
    private static class LogStmtForMessageBuilder implements Builder{

        public String buildDebug(String message) {
            return ".debug(\"" + message + "\")";
        }

        public String buildError(String message) {
            return ".error(\"" + message + "\")";
        }

        public String buildInfo(String message) {
            return ".info(\"" + message + "\")";
        }

        public String buildTrace(String message) {
            return ".trace(\"" + message + "\")";
        }

        public String buildWarning(String message) {
            return ".warn(\"" + message + "\")";
        }
    }
    
    private static class LogStmtForMessageExpBuilder implements Builder{

        public String buildDebug(String message) {
            return ".debug(" + message + ")";
        }

        public String buildError(String message) {
            return ".error(" + message + ")";
        }

        public String buildInfo(String message) {
            return ".info(" + message + ")";
        }

        public String buildTrace(String message) {
            return ".trace(" + message + ")";
        }

        public String buildWarning(String message) {
            return ".warn(" + message + ")";
        }
    }

    private static class LogStmtForThrowableBuilder implements Builder{

        private String throwableExp;
        
        private LogStmtForThrowableBuilder(String throwableExp){
            this.throwableExp = throwableExp;
        }
        
        public String buildDebug(String message) {
            return ".debug(" + message + ", " + throwableExp + ")";
        }

        public String buildError(String message) {
            return ".error(" + message + ", " + throwableExp + ")";
        }

        public String buildInfo(String message) {
            return ".info(" + message + ", " + throwableExp + ")";
        }

        public String buildTrace(String message) {
            return ".trace(" + message + ", " + throwableExp + ")";
        }

        public String buildWarning(String message) {
            return ".warn(" + message + ", " + throwableExp + ")";
        }
    }

}
