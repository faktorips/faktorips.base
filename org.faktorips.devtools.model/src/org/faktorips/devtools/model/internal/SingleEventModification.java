/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * An internal abstract class that is used to execute modifications on
 * {@link IIpsObjectPartContainer}s that would otherwise cause multiple {@link ContentChangeEvent}s.
 * To suppress the unwanted events and to fire a single event instead when all the modifications are
 * completed one needs to implement this interface and execute it by means of the
 * {@link IpsModel#executeModificationsWithSingleEvent(SingleEventModification)} method.
 * 
 * @author Peter Kuntz
 */
public abstract class SingleEventModification<T> {

    private final IIpsSrcFile ipsSrcFile;

    public SingleEventModification(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
    }

    /**
     * Is called by the framework. The modifications are supposed to be implemented within this
     * method.
     * 
     * @return true if the modifications have been successful and an event needs to be fired
     *             afterwards
     * 
     * @throws IpsException exceptions within this method
     */
    protected abstract boolean execute() throws IpsException;

    /**
     * Returns the {@link ContentChangeEvent} that is fired after the {@link #execute()} method has
     * been executed. By default a whole content change event is fired.
     */
    protected ContentChangeEvent modificationEvent() {
        return ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile());
    }

    /**
     * Returns the result of the execution if available. Returns <code>null</code> if the execution
     * doesn't have a result that needs to be returned. The default implementation always returns
     * null.
     * 
     * @return the result of the execution or <code>null</code> if none needs to be returned
     */
    protected T getResult() {
        return null;
    }

    /**
     * @return Returns the ipsSrcFile.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

}
