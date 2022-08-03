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

import java.util.List;

import org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector;

/**
 * An implementation of the <code>IIpsLoggingFrameworkConnector</code> interface that connects to
 * the Log4j logging framework.
 * 
 * @author Peter Erzberger
 * @deprecated since 21.12
 */
@Deprecated(since = "21.12")
public class Log4jLoggingFrameworkConnector implements IIpsLoggingFrameworkConnector {

    public static final String LOG4J_LEVEL_QNAME = "org.apache.log4j.Level"; //$NON-NLS-1$

    public static final String LOG4J_LOGGER_QNAME = "org.apache.log4j.Logger"; //$NON-NLS-1$

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
        StringBuilder sb = new StringBuilder();
        sb.append(loggerInstanceExp);

        if (IIpsLoggingFrameworkConnector.LEVEL_TRACE == level) {
            sb.append(builder.buildTrace(message));
        } else if (IIpsLoggingFrameworkConnector.LEVEL_DEBUG == level) {
            sb.append(builder.buildDebug(message));
        } else if (IIpsLoggingFrameworkConnector.LEVEL_INFO == level) {
            sb.append(builder.buildInfo(message));
        } else if (IIpsLoggingFrameworkConnector.LEVEL_WARNING == level) {
            sb.append(builder.buildWarning(message));
        } else if (IIpsLoggingFrameworkConnector.LEVEL_ERROR == level) {
            sb.append(builder.buildError(message));
        } else {
            throw new IllegalArgumentException("The value of the parameter level is not valid. " //$NON-NLS-1$
                    + "Use the level constants of " + IIpsLoggingFrameworkConnector.class + "."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return sb.toString();
    }

    @Override
    public String getLogConditionExp(int level, String loggerInstanceExp, List<String> usedClasses) {
        return build(level, loggerInstanceExp, null, new LogConditionExpBuilder(usedClasses));
    }

    @Override
    public String getLogStmtForMessage(int level,
            String msgConstant,
            String loggerInstanceExp,
            List<String> usedClasses) {
        return build(level, loggerInstanceExp, msgConstant, new LogStmtForMessageBuilder());
    }

    @Override
    public String getLogStmtForMessageExp(int level,
            String msgExp,
            String loggerInstanceExp,
            List<String> usedClasses) {
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
        return LOG4J_LOGGER_QNAME;
    }

    @Override
    public String getLoggerInstanceStmt(String scopeExp, List<String> usedClasses) {
        usedClasses.add(getLoggerClassName());
        return "Logger.getLogger(" + scopeExp + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private interface Builder {

        String buildInfo(String message);

        String buildTrace(String message);

        String buildDebug(String message);

        String buildWarning(String message);

        String buildError(String message);

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
            usedClasses.add(LOG4J_LEVEL_QNAME);
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
            usedClasses.add(LOG4J_LEVEL_QNAME);
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
