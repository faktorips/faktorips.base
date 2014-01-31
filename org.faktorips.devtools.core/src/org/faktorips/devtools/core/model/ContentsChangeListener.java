/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;


/**
 * A listener that listens to changes in source files.
 * 
 * @author Jan Ortmann
 */
public interface ContentsChangeListener {

    /**
     * Notifies the listener that an object has changed.
     * 
     */
    public void contentsChanged(ContentChangeEvent event);

}
