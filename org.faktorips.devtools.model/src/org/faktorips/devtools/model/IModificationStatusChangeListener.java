/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

/**
 * A listener for changes to modification status changes of IPS source files.
 * 
 * @author Jan Ortmann
 */
@FunctionalInterface
public interface IModificationStatusChangeListener {

    /**
     * Notifies the listener that the modification status of an IPS source file has changed.
     * 
     * @param event The event with the detailed information, is never <code>null</code>.
     */
    void modificationStatusHasChanged(ModificationStatusChangedEvent event);

}
