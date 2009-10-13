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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * This interface is implemented by every java class of the ips meta model representing a meta class
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
     * or not. If the meta class does not support sub types, the parameter will be ignored.
     * 
     * @param ipsProject the project in which the search is started. Because of project references
     *            are directed, the method has to find all projects, referencing this
     *            <code>ipsProject</code>.
     * @param includeSubtypes for meta classes that support subtype hierarchy: true to include
     *            objects from meta class subtypes
     * @return An array of <code>IIpsSrcFile</code>s containing all meta objects that are instances
     *         of this meta class
     * @throws CoreException
     */
    public IIpsSrcFile[] findAllMetaObjectSrcFiles(IIpsProject ipsProject, boolean includeSubtypes)
            throws CoreException;

}
