/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

public interface ILibraryIpsPackageFragment extends IIpsPackageFragment {

    void findIpsObjects(IpsObjectType type, List<IIpsObject> result) throws IpsException;

    void findIpsSourceFiles(IpsObjectType type, List<IIpsSrcFile> result) throws IpsException;

    void findIpsObjects(List<IIpsObject> result) throws IpsException;

}