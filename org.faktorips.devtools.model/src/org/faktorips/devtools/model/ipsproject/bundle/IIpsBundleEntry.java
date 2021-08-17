/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject.bundle;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsLibraryEntry;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;

public interface IIpsBundleEntry extends IIpsLibraryEntry {

    boolean exists(QualifiedNameType qnt) throws CoreException;

    IIpsStorage getIpsStorage();

}