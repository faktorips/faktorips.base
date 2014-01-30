/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * An event that signals the change of an IPS source file's modification status from modifier to
 * unmodified or vice versa.
 * 
 * @author Jan Ortmann
 */
public class ModificationStatusChangedEvent {

    private IIpsSrcFile file;

    public ModificationStatusChangedEvent(IIpsSrcFile file) {
        this.file = file;
    }

    /**
     * Returns the file which modification status has changed.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return file;
    }

}
