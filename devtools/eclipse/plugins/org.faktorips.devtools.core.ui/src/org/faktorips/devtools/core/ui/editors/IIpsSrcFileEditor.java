/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Interface for all kinds of editors that editing an {@link IIpsSrcFile}
 * 
 * @author dirmeier
 */
public interface IIpsSrcFileEditor {

    /**
     * Getting the ipsSrcFile edited by this editor
     * 
     * @return the file actually edited
     */
    IIpsSrcFile getIpsSrcFile();

}
