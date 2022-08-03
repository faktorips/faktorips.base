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

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This interface is implemented by every java class of the IPS meta model representing a meta
 * object that is derived from a meta class
 * 
 * e.g. IProductCmpt, IEnumContent, ITableContents and ITestCase.
 */
public interface IIpsMetaObject extends IFixDifferencesToModelSupport {

    /**
     * This method returns the qualified name of the meta class, defining this meta object
     * 
     * @return the qualified name of its meta object
     */
    String getMetaClass();

    /**
     * This method finds the IPS source file of the meta class, defining this meta object
     * 
     * @param ipsProject the ipsProject used to find the meta class
     * 
     * @return Returns the <code>IIpsSrcFile</code> for the meta class of this meta object
     */
    IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) throws IpsException;

}
