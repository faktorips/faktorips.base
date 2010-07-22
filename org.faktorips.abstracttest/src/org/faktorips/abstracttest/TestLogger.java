/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.Bundle;

/**
 * An implementation of the {@link ILog} interface for testing purposes. Not all of the methods are
 * implemented but can be implemented as needed. Those methods that are not implemented throw a
 * RuntimeException.
 * 
 * @author Peter Erzberger
 */
public class TestLogger implements ILog {

    private List<IStatus> logEntries = new ArrayList<IStatus>();

    /**
     * Returns the log entries added to this logger.
     */
    public List<IStatus> getLogEntries() {
        return logEntries;
    }

    /**
     * Resets the log entry list.
     */
    public void reset() {
        logEntries.clear();
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public void addLogListener(ILogListener listener) {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public Bundle getBundle() {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(IStatus status) {
        logEntries.add(status);
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public void removeLogListener(ILogListener listener) {
        throw new RuntimeException("Not implemented yet.");
    }

}
