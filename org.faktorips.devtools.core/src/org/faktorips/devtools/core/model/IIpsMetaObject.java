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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This interface is implemented by every java class of the ips meta model representing a meta
 * object that is derived from a meta class
 * 
 * e.g. IProductCmpt, IEnumContent, ITableContents and ITestCase.
 * 
 * @author dirmeier
 */
public interface IIpsMetaObject extends IFixDifferencesToModelSupport, IIpsObject {

    /**
     * This method returns the qualified name of the meta class, defining this meta object
     * 
     * @return the qualified name of its meta object
     */
    public String getMetaClass();

    /**
     * This method finds the IPS source file of the meta class, defining this meta object
     * 
     * @param ipsProject the ipsProject used to find the meta class
     * 
     * @return Returns the <code>IIpsSrcFile</code> for the meta class of this meta object
     */
    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) throws CoreException;

}
