/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.cache;

import java.util.Collection;

import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A Cache providing a list of all product components in an {@link IIpsProject}. The
 * {@link IpsSrcFile}s are identified by their unqualified name.
 * 
 */
public class UnqualifiedNameCache extends ProductCmptCache {

    public UnqualifiedNameCache(IIpsProject ipsProject) {
        super(ipsProject);
    }

    public Collection<IIpsSrcFile> findProductCmptByUnqualifiedName(String unqualifiedName) {
        return super.findProductCmptsByKey(unqualifiedName);
    }

    @Override
    protected String getKey(IIpsSrcFile productCmpt) {
        return productCmpt.getIpsObjectName();
    }

}
