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

package org.faktorips.devtools.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.Bundle;

/**
 * An implementation of the {@link ILog} interface for testing purposes. Not all of the methods are implemented but can be implemented
 * as needed. Those methods that are not implemented throw a RuntimeException.
 * 
 * @author Peter Erzberger
 */
public class TestLogger implements ILog {

    private List logEntries = new ArrayList();
    
    /**
     * Returns the log entries added to this logger.
     */
    public List getLogEntries(){
        return logEntries;
    }
    
    /**
     * Resets the log entry list.
     */
    public void reset(){
        logEntries.clear();
    }
    
    /**
     * Throws RuntimeException
     */
    public void addLogListener(ILogListener listener) {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * Throws RuntimeException
     */
    public Bundle getBundle() {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    public void log(IStatus status) {
        logEntries.add(status);
    }

    /**
     * Throws RuntimeException
     */
    public void removeLogListener(ILogListener listener) {
        throw new RuntimeException("Not implemented yet.");
    }

}
