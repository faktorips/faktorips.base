/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Collection;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * A Cache providing a list of all product components in an {@link IpsProject}. The
 * {@link IpsSrcFile}s are identified by their unqualified name.
 * 
 */
public class UnqualifiedNameCache extends ProductCmptCache {

    public UnqualifiedNameCache(IpsProject ipsProject) {
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
