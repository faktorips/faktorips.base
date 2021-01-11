/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
