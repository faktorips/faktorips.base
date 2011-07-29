/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.scope;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * The IIpsSearchScope defines, which IpsSrcFile the search should consider.
 * 
 * @author dicker
 */
public interface IIpsSearchScope {

    /**
     * @return Set of srcFile, which are related in this Scope
     */
    public Set<IIpsSrcFile> getSelectedIpsSrcFiles() throws CoreException;

    /**
     * returns the description of the scope
     * 
     * @return String
     */
    public String getScopeDescription();
}
