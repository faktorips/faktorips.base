/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Collection;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.util.MultiMap;

/**
 * A Cache providing a list of all {@link IIpsSrcFile}s of a specific {@link IpsObjectType} in an
 * {@link IpsProject}. The {@link IpsSrcFile}s are identified by their unqualified name. Currently
 * this Cache provides a list only for {@link IProductCmpt}s.
 * 
 */
public class UnqualifiedNameCache {

    private IpsProject ipsProject;

    private MultiMap<String, IIpsSrcFile> prodCmptIpsSrcFilesMap = new MultiMap<String, IIpsSrcFile>();

    public UnqualifiedNameCache(IpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    public Collection<IIpsSrcFile> findProductCmptByUnqualifiedName(String unqualifiedName) {
        Collection<IIpsSrcFile> result = prodCmptIpsSrcFilesMap.get(unqualifiedName);
        if (result.isEmpty() || !checkAllIpsSrcFilesExists(result)) {
            initProdCmptIpsSrcFilesMap();
            result = prodCmptIpsSrcFilesMap.get(unqualifiedName);
        }
        return result;
    }

    private boolean checkAllIpsSrcFilesExists(Collection<IIpsSrcFile> result) {
        for (IIpsSrcFile ipsSrcFile : result) {
            if (!ipsSrcFile.exists()) {
                return false;
            }
        }
        return true;
    }

    private void initProdCmptIpsSrcFilesMap() {
        prodCmptIpsSrcFilesMap.clear();
        List<IIpsSrcFile> allProdCmptIpsSrcFiles = ipsProject.findAllIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : allProdCmptIpsSrcFiles) {
            prodCmptIpsSrcFilesMap.put(ipsSrcFile.getIpsObjectName(), ipsSrcFile);
        }
    }

}
