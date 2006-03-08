/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Extension of status that sets the correct plugin id and provides some
 * conveniance constructors.
 * 
 * @author Jan Ortmann 
 */
public class IpsStatus extends Status {

    /**
     * Creates a new error status based on the given throwable.
     */
    public IpsStatus(Throwable throwable) {
        super(IStatus.ERROR, IpsPlugin.PLUGIN_ID, 0, 
                throwable.getMessage()!=null?throwable.getMessage():"", throwable); //$NON-NLS-1$
        // use the throwable's message as the Errordialg makes no use of the throwable
    }
    
    /**
     * Creates a new error status based on the given throwable.
     */
    public IpsStatus(String msg) {
        super(IStatus.ERROR, IpsPlugin.PLUGIN_ID, 0, msg, null);
    }
    
    /**
     * Creates a new error status based on the given message and throwable.
     */
    public IpsStatus(String msg, Throwable t) {
        super(IStatus.ERROR, IpsPlugin.PLUGIN_ID, 0, msg, t);
    }
    
    /**
     * Creates a new status.
     */
    public IpsStatus(int severity, String message, Throwable exception) {
        super(severity, IpsPlugin.PLUGIN_ID, 0, message, exception);
    }
    
    /**
     * Creates a new status.
     */
    public IpsStatus(int severity, int code,
            String message, Throwable exception) {
        super(severity, IpsPlugin.PLUGIN_ID, code, message, exception);
    }

}
