/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
