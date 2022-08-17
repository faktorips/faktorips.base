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

import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;

/**
 * The {@link DeepCopyWizard} uses an {@link IDeepCopySmartModeBehavior} to initialise the
 * {@link CopyOrLink} state of elements when in {@link IpsPreferences#isCopyWizardModeSmartmode()
 * Smart Mode}.
 * 
 * @since 3.22
 */
public interface IDeepCopySmartModeBehavior {

    String CONFIG_ELEMENT_ID_SMART_MODE_BEHAVIOR = "smartModeBehavior"; //$NON-NLS-1$

    /**
     * Determines the initial {@link CopyOrLink} state for the given
     * {@link IProductCmptStructureReference reference} when the copied product's package fragment
     * root is the given one.
     * <p>
     * This method will not be called for the root component, as it is always copied.
     * <p>
     * This method will only be called for {@link IProductCmptReference IProductCmptReferences} and
     * {@link IProductCmptStructureTblUsageReference IProductCmptStructureTblUsageReferences}, as
     * they are the only kinds of references for which the link/copy status can be chosen.
     * 
     * @param root the root package fragment of the copied product
     * @param reference the node to be copied/linked
     * @return whether the reference should be copied or linked
     */
    CopyOrLink getCopyOrLink(IIpsPackageFragmentRoot root, IProductCmptStructureReference reference);
}
