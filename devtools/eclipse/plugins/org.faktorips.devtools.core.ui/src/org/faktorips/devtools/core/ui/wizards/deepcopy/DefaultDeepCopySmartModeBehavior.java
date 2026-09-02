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
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;

/**
 * Default implementation for {@link IDeepCopySmartModeBehavior} that creates copies for elements
 * that lie in the package of the copied root product component or one of its sub-packages, and
 * links to all others (e.g. elements from sibling packages, other package fragment roots or other
 * projects).
 *
 * @since 3.22
 */
public class DefaultDeepCopySmartModeBehavior implements IDeepCopySmartModeBehavior {

    @Override
    public CopyOrLink getCopyOrLink(IIpsPackageFragmentRoot root, IProductCmptStructureReference reference) {
        IIpsObject wrappedIpsObject = reference.getWrappedIpsObject();
        // wrappedIpsObject may be null for ProductCmptStructureTblUsageReference
        if (wrappedIpsObject != null && wrappedIpsObject.getIpsPackageFragment() != null) {
            IIpsPackageFragment referencePackage = wrappedIpsObject.getIpsPackageFragment();
            IIpsPackageFragmentRoot referencePackageFragmentRoot = referencePackage.getRoot();
            if (root.equals(referencePackageFragmentRoot) && isSamePackageOrSubPackage(referencePackage, reference)) {
                return CopyOrLink.COPY;
            } else {
                return CopyOrLink.LINK;
            }
        } else {
            return CopyOrLink.UNDEFINED;
        }
    }

    /**
     * Returns {@code true} if the given reference package is the package of the copied root
     * product component or a sub-package of it, so that the package structure below the root is
     * preserved for elements that are copied along with it.
     */
    private boolean isSamePackageOrSubPackage(IIpsPackageFragment referencePackage,
            IProductCmptStructureReference reference) {
        IIpsPackageFragment rootPackage = reference.getStructure().getRoot().getProductCmpt()
                .getIpsPackageFragment();
        if (rootPackage == null) {
            return false;
        }
        return rootPackage.getRelativePath().isPrefixOf(referencePackage.getRelativePath());
    }

}
