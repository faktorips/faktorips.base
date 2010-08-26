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

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;

/**
 * An implementation of the <code>IIpsLoggingFrameworkConnector</code> interface that connects to
 * the Log4j logging framework.
 * 
 * @author Peter Erzberger
 */
public class Log4jLoggingFrameworkConnector implements IIpsLoggingFrameworkConnector {

    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    private String build(int level, String loggerInstanceExp, String message, Builder builder) {
        StringBuffer buf = new StringBuffer();
        buf.append(loggerInstanceExp);

        if (IIpsLoggingFrameworkConnector.LEVEL_TRACE == level) {
            buf.append(builder.buildTrace(message));
        } else if (IIpsLoggingFrameworkConnector.LEVEL_DEBUG == level) {
            buf.append(builder.buildDebug(message));
        } else if (IIpsLoggingFrameworkConnector.LEVEL_INFO == level) {
            buf.append(builder.buildInfo(message));
        } else if (IIpsLoggingFrameworkConnector.LEVEL_WARNING == level) {
            buf.append(builder.buildWarning(message));
        } else if (IIpsLoggingFrameworkConnector.LEVEL_ERROR == level) {
            buf.append(builder.buildError(message));
        } else {
            throw new IllegalArgumentException("The value of the parameter level is not valid. " + //$NON-NLS-1$
                    "Use the level constants of " + IIpsLoggingFrameworkConnector.class + "."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return buf.toString();

    }

    @Override
    public String getLogConditionExp(int level, String loggerInstanceExp, List<String> usedClasses) {
        return build(level, loggerInstanceExp, null, new LogConditionExpBuilder(usedClasses));
    }

    @Override
    public String getLogStmtForMessage(int level, String msgConstant, String loggerInstanceExp, List<String> usedClasses) {
        return build(level, loggerInstanceExp, msgConstant, new LogStmtForMessageBuilder());
    }

    @Override
    public String getLogStmtForMessageExp(int level, String msgExp, String loggerInstanceExp, List<String> usedClasses) {
        return build(level, loggerInstanceExp, msgExp, new LogStmtForMessageExpBuilder());
    }

    @Override
    public String getLogStmtForThrowable(int level,
            String msgExp,
            String throwableExp,
            String loggerInstanceExp,
            List<String> usedClasses) {
        return build(level, loggerInstanceExp, msgExp, new LogStmtForThrowableBuilder(throwableExp));
    }

    @Override
    public String getLoggerClassName() {
        return Logger.class.getName();
    }

    @Override
    public String getLoggerInstanceStmt(String scopeExp, List<String> usedClasses) {
        usedClasses.add(Logger.class.getName());
        return "Logger.getLogger(" + scopeExp + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static interface Builder {

        public String buildInfo(String message);

        public String buildTrace(String message);

        public String buildDebug(String message);

        public String buildWarning(String message);

        public String buildError(String message);

    }

    private static class LogConditionExpBuilder implements Builder {

        private List<String> usedClasses;

        private LogConditionExpBuilder(List<String> usedClasses) {
            this.usedClasses = usedClasses;
        }

        @Override
        public String buildDebug(String message) {
            return ".isDebugEnabled()"; //$NON-NLS-1$
        }

        @Override
        public String buildError(String message) {
            usedClasses.add(Level.class.getName());
            return ".isEnabledFor(Level.ERROR)"; //$NON-NLS-1$
        }

        @Override
        public String buildInfo(String message) {
            return ".isInfoEnabled()"; //$NON-NLS-1$
        }

        @Override
        public String buildTrace(String message) {
            return ".isTraceEnabled()"; //$NON-NLS-1$
        }

        @Override
        public String buildWarning(String message) {
            usedClasses.add(Level.class.getName());
            return ".isEnabledFor(Level.WARN)"; //$NON-NLS-1$
        }

    }

    private static class LogStmtForMessageBuilder implements Builder {

        @Override
        public String buildDebug(String message) {
            return ".debug(\"" + message + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String buildError(String message) {
            return ".error(\"" + message + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String buildInfo(String message) {
            return ".info(\"" + message + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String buildTrace(String message) {
            return ".trace(\"" + message + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String buildWarning(String message) {
            return ".warn(\"" + message + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    private static class LogStmtForMessageExpBuilder implements Builder {

        @Override
        public String buildDebug(String message) {
            return ".debug(" + message + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String buildError(String message) {
            return ".error(" + message + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String buildInfo(String message) {
            return ".info(" + message + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String buildTrace(String message) {
            return ".trace(" + message + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String buildWarning(String message) {
            return ".warn(" + message + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    private static class LogStmtForThrowableBuilder implements Builder {

        private String throwableExp;

        private LogStmtForThrowableBuilder(String throwableExp) {
            this.throwableExp = throwableExp;
        }

        @Override
        public String buildDebug(String message) {
            return ".debug(" + message + ", " + throwableExp + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        @Override
        public String buildError(String message) {
            return ".error(" + message + ", " + throwableExp + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        @Override
        public String buildInfo(String message) {
            return ".info(" + message + ", " + throwableExp + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        @Override
        public String buildTrace(String message) {
            return ".trace(" + message + ", " + throwableExp + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        @Override
        public String buildWarning(String message) {
            return ".warn(" + message + ", " + throwableExp + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

    }

}
