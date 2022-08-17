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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the <code>IIpsLoggingFrameworkConnector</code> interface that connects to
 * the java.util.logging logging framework.
 * 
 * @author Peter Erzberger
 * @deprecated since 21.12.
 */
@Deprecated(since = "21.12")
public class JavaUtilLoggingFrameworkConnector implements IIpsLoggingFrameworkConnector {

    private String id = ""; //$NON-NLS-1$

    private String getLevelExp(int level) {

        if (level == IIpsLoggingFrameworkConnector.LEVEL_INFO) {
            return "Level.INFO"; //$NON-NLS-1$
        }
        if (level == IIpsLoggingFrameworkConnector.LEVEL_WARNING) {
            return "Level.WARNING"; //$NON-NLS-1$
        }
        if (level == IIpsLoggingFrameworkConnector.LEVEL_ERROR) {
            return "Level.SEVERE"; //$NON-NLS-1$
        }
        if (level == IIpsLoggingFrameworkConnector.LEVEL_DEBUG) {
            return "Level.FINE"; //$NON-NLS-1$
        }
        if (level == IIpsLoggingFrameworkConnector.LEVEL_TRACE) {
            return "Level.FINEST"; //$NON-NLS-1$
        }
        throw new IllegalArgumentException("The specified logging level is not defined: " + level); //$NON-NLS-1$
    }

    @Override
    public String getLogConditionExp(int level, String loggerExpression, List<String> usedClasses) {
        usedClasses.add(Level.class.getName());
        return loggerExpression + ".isLoggable(" + getLevelExp(level) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String getLevelMethodName(int level) {
        if (level == IIpsLoggingFrameworkConnector.LEVEL_INFO) {
            return "info"; //$NON-NLS-1$
        }
        if (level == IIpsLoggingFrameworkConnector.LEVEL_WARNING) {
            return "warning"; //$NON-NLS-1$
        }
        if (level == IIpsLoggingFrameworkConnector.LEVEL_ERROR) {
            return "severe"; //$NON-NLS-1$
        }
        if (level == IIpsLoggingFrameworkConnector.LEVEL_DEBUG) {
            return "fine"; //$NON-NLS-1$
        }
        if (level == IIpsLoggingFrameworkConnector.LEVEL_TRACE) {
            return "finest"; //$NON-NLS-1$
        }
        throw new IllegalArgumentException("The specified logging level is not defined: " + level); //$NON-NLS-1$
    }

    @Override
    public String getLogStmtForMessage(int level,
            String msgConstant,
            String loggerInstanceExp,
            List<String> usedClasses) {
        StringBuilder sb = new StringBuilder();
        sb.append(loggerInstanceExp);
        sb.append("."); //$NON-NLS-1$
        sb.append(getLevelMethodName(level));
        sb.append("(\""); //$NON-NLS-1$
        sb.append(msgConstant);
        sb.append("\")"); //$NON-NLS-1$
        return sb.toString();
    }

    @Override
    public String getLogStmtForMessageExp(int level,
            String msgExp,
            String loggerInstanceExp,
            List<String> usedClasses) {
        StringBuilder sb = new StringBuilder();
        sb.append(loggerInstanceExp);
        sb.append("."); //$NON-NLS-1$
        sb.append(getLevelMethodName(level));
        sb.append("("); //$NON-NLS-1$
        sb.append(msgExp);
        sb.append(")"); //$NON-NLS-1$
        return sb.toString();
    }

    @Override
    public String getLogStmtForThrowable(int level,
            String msgExp,
            String throwableExp,
            String loggerInstanceExp,
            List<String> usedClasses) {
        usedClasses.add(Level.class.getName());
        StringBuilder sb = new StringBuilder();
        sb.append(loggerInstanceExp);
        sb.append(".log("); //$NON-NLS-1$
        sb.append(getLevelExp(level));
        sb.append(", "); //$NON-NLS-1$
        sb.append(msgExp);
        sb.append(", "); //$NON-NLS-1$
        sb.append(throwableExp);
        sb.append(")"); //$NON-NLS-1$
        return sb.toString();
    }

    @Override
    public String getLoggerClassName() {
        return Logger.class.getName();
    }

    @Override
    public String getLoggerInstanceStmt(String scopeExp, List<String> usedClasses) {
        usedClasses.add(Logger.class.getName());
        StringBuilder sb = new StringBuilder();
        sb.append("Logger.getLogger("); //$NON-NLS-1$
        sb.append(scopeExp);
        sb.append(")"); //$NON-NLS-1$
        return sb.toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        ArgumentCheck.notNull(id);
        this.id = id;
    }

}
