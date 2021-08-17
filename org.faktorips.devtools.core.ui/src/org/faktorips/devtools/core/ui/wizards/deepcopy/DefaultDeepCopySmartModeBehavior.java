/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;

/**
 * Default implementation for {@link IDeepCopySmartModeBehavior} that creates copies for elements
 * from the original {@link IIpsPackageFragmentRoot} and links to all others.
 * 
 * @since 3.22
 */
public class DefaultDeepCopySmartModeBehavior implements IDeepCopySmartModeBehavior {

    @Override
    public CopyOrLink getCopyOrLink(IIpsPackageFragmentRoot root, IProductCmptStructureReference reference) {
        IIpsObject wrappedIpsObject = reference.getWrappedIpsObject();
        // wrappedIpsObject may be null for ProductCmptStructureTblUsageReference
        if (wrappedIpsObject != null && wrappedIpsObject.getIpsPackageFragment() != null) {
            IIpsPackageFragmentRoot referencePackageFragmentRoot = wrappedIpsObject.getIpsPackageFragment().getRoot();
            if (root.equals(referencePackageFragmentRoot)) {
                return CopyOrLink.COPY;
            } else {
                return CopyOrLink.LINK;
            }
        } else {
            return CopyOrLink.UNDEFINED;
        }
    }

}
