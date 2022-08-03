/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Objects wrapping an {@link IIpsSrcFile} should implement this interface to enable eclipse getting
 * the wrapped {@link IIpsSrcFile}. e.g. this is the case for structural model elements as well as
 * viewer items providing additional information for an IPS source file.
 * 
 * @author dirmeier
 */
public interface IIpsSrcFileWrapper extends IAdaptable {

    /**
     * Return the wrapped {@link IIpsSrcFile}
     * 
     * @return the wrapped {@link IIpsSrcFile}
     */
    IIpsSrcFile getWrappedIpsSrcFile();

}
