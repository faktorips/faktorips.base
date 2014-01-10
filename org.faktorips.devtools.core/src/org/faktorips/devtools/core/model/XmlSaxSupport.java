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

package org.faktorips.devtools.core.model;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

/**
 * An interface that marks an object as being able to read via SAX.
 * 
 * @author Joerg Ortmann
 */
public interface XmlSaxSupport {

    /**
     * (Re)Initializes the object's state with the input stream.
     * 
     * @param is input stream
     */
    public void initFromInputStream(InputStream is) throws CoreException;

    /**
     * Adds the given extension property. If the extension property not exists as definitions then
     * the property will be ignored.
     * 
     * @param propertyId The id of the extension property
     * @param extPropertyValue The value of the extension property
     */
    public void addExtensionProperty(String propertyId, String extPropertyValue);

}
