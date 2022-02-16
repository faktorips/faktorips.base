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

import java.util.Collection;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * 
 * This interface is implemented by every java class of the IPS meta model representing a meta class
 * that is able to be instantiated
 * 
 * e.g. IProductCmptType, IEnumType, ITableStructure and ITestCaseType.
 * 
 * @author dirmeier
 * 
 */
public interface IIpsMetaClass extends IIpsObject {

    /**
     * This method returns an array of <code>IIpsSrcFile</code>s for all meta objects found for this
     * meta class. When the meta class supports a type hierarchy, the parameter
     * <code>includeSubtypes</code> specifies whether to search in sub types for further instances
     * or not. If the meta class does not support sub types, the parameter will be ignored. The
     * search finds meta classes in all projects referencing the ipsProject of this type
     * 
     * @param includeSubtypes for meta classes that support sub type hierarchy: true to include
     *            objects from meta class sub types
     * 
     * @return An array of <code>IIpsSrcFile</code>s containing all meta objects that are instances
     *         of this meta class
     */
    public Collection<IIpsSrcFile> searchMetaObjectSrcFiles(boolean includeSubtypes) throws IpsException;

}
