/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.ALogListener;
import org.faktorips.devtools.abstraction.AWrapper;

public class AEclipseLog extends AWrapper<ILog> implements ALog {

    private final Map<ALogListener, ILogListener> listeners = new WeakHashMap<>();

    public AEclipseLog(ILog wrapped) {
        super(wrapped);
    }

    @Override
    public void log(IStatus status) {
        unwrap().log(status);
    }

    @Override
    public void addLogListener(ALogListener listener) {
        ILogListener iLogListener = listeners.computeIfAbsent(listener, l -> l::logging);
        unwrap().addLogListener(iLogListener);
    }

    @Override
    public void removeLogListener(ALogListener listener) {
        ILogListener iLogListener = listeners.get(listener);
        if (iLogListener != null) {
            unwrap().removeLogListener(iLogListener);
        }
    }

}
