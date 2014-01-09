/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

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
    public IIpsSrcFile getWrappedIpsSrcFile();

}
