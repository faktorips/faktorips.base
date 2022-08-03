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
 * A listener that listens to changes in source files.
 * 
 * @author Jan Ortmann
 */
@FunctionalInterface
public interface ContentsChangeListener {

    /**
     * Notifies the listener that an object has changed.
     * 
     */
    void contentsChanged(ContentChangeEvent event);

}
