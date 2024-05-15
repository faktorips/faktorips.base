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

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.util.collections.IdentityHashSet;

/**
 * This tree status holds a map of {@link IProductCmpt} to a Map of {@link IProductCmptLink} and
 * {@link LinkStatus} as value. Every entry in the outer map represents a part of the structure of
 * the product component. This structure is useful, because for every {@link IProductCmpt} the
 * status of all children links have to be the same. Using this map, you could only store those
 * equal structures.
 *
 * @author dirmeier
 */
public class DeepCopyTreeStatus extends PresentationModelObject {

    private Map<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> treeStatus;

    /**
     * A map of {@link IProductCmptLink} caching all associations (not composition or aggregations).
     * The value of {@link IProductCmpt} is the target of the link, hold in the map for performance
     * use.
     */
    private Map<IProductCmptLink, IProductCmpt> associationLinks;

    private final IpsPreferences ipsPreferences;

    private IIpsPackageFragmentRoot root;

    private final IDeepCopySmartModeBehavior deepCopySmartModeBehavior;

    public DeepCopyTreeStatus() {
        this(IpsPlugin.getDefault().getIpsPreferences(), IpsUIPlugin.getDefault().getDeepCopySmartModeBehavior());
    }

    /* for testing */
    DeepCopyTreeStatus(IpsPreferences ipsPreferences, IDeepCopySmartModeBehavior deepCopySmartModeBehavior) {
        this.ipsPreferences = ipsPreferences;
        this.deepCopySmartModeBehavior = deepCopySmartModeBehavior;
    }

    /**
     * Initializes the tree status for all references in the structure with default values
     *
     * @param structure the structure containing all references a status should be initialized for
     */
    public void initialize(IProductCmptTreeStructure structure) {
        treeStatus = new HashMap<>();
        associationLinks = new HashMap<>();
        IProductCmpt rootProductCmpt = structure.getRoot().getProductCmpt();
        root = rootProductCmpt.getIpsPackageFragment().getRoot();
        HashMap<IProductCmptLink, IProductCmpt> associationLinksCopy = new HashMap<>();
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference instanceof IProductCmptTypeAssociationReference) {
                // no status for associations is needed
                continue;
            } else {
                LinkStatus status = getStatus(reference);
                if (reference instanceof IProductCmptReference cmptReference) {
                    IProductCmptTypeAssociationReference parent = cmptReference.getParent();
                    if (parent != null && parent.getAssociation().isAssoziation()) {
                        // default for associated product components is linked, not copy
                        // in the getter method the status is verified: due to FIPS-3, we want to
                        // copy references that target a product component which is also copied
                        status.setCopyOrLink(CopyOrLink.LINK);
                        // do not add to real associationLinks map while initialization because
                        // checking for association copy is not very efficient and we do not have to
                        // check here
                        associationLinksCopy.put(cmptReference.getLink(), cmptReference.getProductCmpt());
                    }
                }
            }
        }
        associationLinks = associationLinksCopy;
    }

    public CopyOrLink getCopyOrLinkStatus(IProductCmptStructureReference reference) {
        return getStatus(reference).getCopyOrLink();
    }

    /**
     * Getting the status for a {@link IProductCmptStructureReference}. If there is no such status,
     * a new status with default values is created. Never returns null.
     *
     * @param reference the link you want to get the status for
     * @return The status for the link maybe a new one with default values
     */
    private LinkStatus getStatus(IProductCmptStructureReference reference) {
        IIpsObjectPart part = reference.getWrapped();
        IProductCmpt parent = getProductCmpt(part);
        Map<IIpsObjectPart, LinkStatus> statusMap = getStatusMap(parent);
        LinkStatus linkStatus = statusMap.get(part);
        if (linkStatus == null) {
            // if there is no status yet, create a new one with default values and return it
            return updateLinkStatusFor(reference, null, null);
        }
        if (associationLinks.containsKey(part)) {
            // FIPS-3: Associations which target is copied by this deep copy operation should also
            // be copied instead of linked
            return getStatusForAssociation(part, linkStatus, reference.getStructure());
        }
        return linkStatus;
    }

    private LinkStatus getStatusForAssociation(IIpsObjectPart part,
            LinkStatus linkStatus,
            IProductCmptTreeStructure structure) {
        if (isProductCmptCopied(part, structure)) {
            // found the same product component that is copied
            return new LinkStatus(linkStatus.getIpsObjectPart(), linkStatus.getTarget(), linkStatus.isChecked(),
                    CopyOrLink.COPY);
        }
        return linkStatus;
    }

    private boolean isProductCmptCopied(IIpsObjectPart part, IProductCmptTreeStructure structure) {
        IProductCmpt target = associationLinks.get(part);
        Set<IProductCmptStructureReference> set = structure.toSet(false);
        for (IProductCmptStructureReference reference : set) {
            if ((reference instanceof IProductCmptTypeAssociationReference)
                    || (reference instanceof IProductCmptReference
                            && associationLinks.containsKey(reference.getWrapped()))) {
                continue;
            }
            IProductCmpt parent = getProductCmpt(reference.getWrapped());
            LinkStatus linkStatus = getStatusMap(parent).get(reference.getWrapped());
            if (isEnabled(reference) && linkStatus.getCopyOrLink() == CopyOrLink.COPY) {
                if (reference.getWrappedIpsObject().equals(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<IIpsObjectPart, LinkStatus> getStatusMap(IProductCmpt parent) {
        return treeStatus.computeIfAbsent(parent, $ -> new HashMap<>());
    }

    /**
     * Updates the link status for the given reference with the values for "checked" and
     * "copyOrLink". Passing <code>null</code> for "checked" or "copyOrLink" preserves the value set
     * in the existing link status. If there is no existing link status, a new one is created. If,
     * in this case, <code>null</code> is passed for "checked" and "copyOrLink", the new
     * {@link LinkStatus} will be initialized with the default values <code>true</code> and COPY
     * respectively.
     *
     * @param reference The {@link IIpsObjectPart} the status should be updated for
     * @param checked the checked status or null to preserve the previous value (if any)
     * @param copyOrLink the copy or link status or null to preserve the previous value (if any)
     * @return the updated or newly created {@link LinkStatus}
     */
    private LinkStatus updateLinkStatusFor(IProductCmptStructureReference reference,
            Boolean checked,
            CopyOrLink copyOrLink) {
        CopyOrLink copyOrLinkStatus = copyOrLink;
        IIpsObjectPart part = reference.getWrapped();
        IProductCmpt parent = getProductCmpt(part);
        Map<IIpsObjectPart, LinkStatus> statusMap = getStatusMap(parent);
        LinkStatus linkStatus = statusMap.get(part);

        Boolean checkedStatus = checked;
        if (linkStatus == null) {
            if (checkedStatus == null) {
                checkedStatus = true;
            }
            if (copyOrLinkStatus == null) {
                copyOrLinkStatus = getInitCopyOrLinkFromPreferences(reference);
            }
            linkStatus = new LinkStatus(reference.getWrapped(), reference.getWrappedIpsObject(), checkedStatus,
                    copyOrLinkStatus);
        }
        if (checkedStatus != null) {
            if (reference.isRoot()) {
                linkStatus.setChecked(true);
            } else {
                linkStatus.setChecked(checkedStatus);
            }
        }
        if (copyOrLinkStatus != null) {
            linkStatus.setCopyOrLink(copyOrLinkStatus);
        }
        statusMap.put(part, linkStatus);
        return linkStatus;
    }

    private CopyOrLink getInitCopyOrLinkFromPreferences(IProductCmptStructureReference reference) {
        if (reference.isRoot()) {
            return CopyOrLink.COPY;
        }
        CopyOrLink copyOrLinkMode;
        if (ipsPreferences.isCopyWizardModeCopy()) {
            copyOrLinkMode = CopyOrLink.COPY;
        } else if (ipsPreferences.isCopyWizardModeLink()) {
            copyOrLinkMode = CopyOrLink.LINK;
        } else {
            copyOrLinkMode = getCopyOrLinkInSmartMode(reference);
        }
        return copyOrLinkMode;
    }

    private CopyOrLink getCopyOrLinkInSmartMode(IProductCmptStructureReference reference) {
        return deepCopySmartModeBehavior.getCopyOrLink(root, reference);
    }

    /**
     * Getting the {@link IProductCmpt} for a part of a {@link IProductCmptGeneration}
     *
     * @throws IllegalArgumentException if the parameter is no part of
     *             {@link IProductCmptGeneration}
     */
    private IProductCmpt getProductCmpt(IIpsObjectPart partOfProductCmpt) {
        if (partOfProductCmpt == null) {
            return null;
        }
        IIpsElement parent = partOfProductCmpt.getIpsObject();
        if (parent instanceof IProductCmpt) {
            return (IProductCmpt)parent;
        } else {
            throw new IllegalArgumentException(partOfProductCmpt.toString() + " is no part of a product component"); //$NON-NLS-1$
        }
    }

    /**
     * Getting the checked state of a reference. For {@link IProductCmptTypeAssociationReference}
     * the checked status is derived from its children. The checked status of the root node is
     * always true.
     *
     * @param reference the reference you want to get the status for
     * @return true if the reference is checked.
     */
    public boolean isChecked(IProductCmptStructureReference reference) {
        if (reference instanceof IProductCmptReference || reference instanceof IProductCmptStructureTblUsageReference) {
            if (reference.isRoot()) {
                return true;
            } else {
                return getStatus(reference).isChecked();
            }
        } else if (reference instanceof IProductCmptTypeAssociationReference associationReference) {
            for (IProductCmptReference child : associationReference.getStructure()
                    .getChildProductCmptReferences(associationReference)) {
                if (isChecked(child)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * Setting the checked status for the give {@link IProductCmptStructureReference}. If the
     * reference is a {@link IProductCmptTypeAssociationReference} the status is set for all
     * children.
     *
     * @param reference the reference you want to change the status for
     * @param value the new status of the reference
     */
    public void setChecked(IProductCmptStructureReference reference, boolean value) {
        boolean oldValue;
        if (reference instanceof IProductCmptReference || reference instanceof IProductCmptStructureTblUsageReference) {
            oldValue = setCheckedInternal(reference, value);
        } else if (reference instanceof IProductCmptTypeAssociationReference associationReference) {
            oldValue = false;
            for (IProductCmptReference child : associationReference.getStructure()
                    .getChildProductCmptReferences(associationReference)) {
                oldValue = setCheckedInternal(child, value) || oldValue;
            }
        } else {
            throw new IllegalArgumentException();
        }
        if (oldValue != value) {
            notifyListeners(new PropertyChangeEvent(reference, LinkStatus.CHECKED, oldValue, value));
        }
    }

    private boolean setCheckedInternal(IProductCmptStructureReference reference, boolean value) {
        boolean oldValue;
        LinkStatus status = getStatus(reference);
        oldValue = status.isChecked();
        updateLinkStatusFor(reference, value, null);
        return oldValue;
    }

    /**
     * Setting the copy or link status for the give {@link IProductCmptStructureReference}. If the
     * reference is a {@link IProductCmptTypeAssociationReference} this method do nothing
     *
     * @param reference the reference you want to change the status for
     * @param value the new status of the reference
     */
    public void setCopyOrLink(IProductCmptStructureReference reference, CopyOrLink value) {
        if (reference instanceof IProductCmptReference || reference instanceof IProductCmptStructureTblUsageReference) {
            LinkStatus status = getStatus(reference);
            CopyOrLink oldValue = status.getCopyOrLink();
            updateLinkStatusFor(reference, null, value);
            if (oldValue != null && !oldValue.equals(value)) {
                notifyListeners(new PropertyChangeEvent(reference, LinkStatus.COPY_OR_LINK, oldValue, value));
            }
        }
    }

    /**
     * Returns true if this reference is enabled. A Reference is enabled when itself and all the
     * parent elements are checked and all parent elements are marked to copy. Returns false if this
     * reference or one of its parents is not checked or any parent is linked.
     *
     * @param reference A reference you want to know the enable state for
     * @return true if this reference is enabled, false otherwise
     */
    public boolean isEnabled(IProductCmptStructureReference reference) {
        boolean enabled = isChecked(reference);
        IProductCmptStructureReference parent = reference.getParent();
        while (enabled && parent != null) {
            if (!(parent instanceof IProductCmptTypeAssociationReference)) {
                LinkStatus status = getStatus(parent);
                enabled = enabled && status.isChecked() && status.getCopyOrLink() == CopyOrLink.COPY;
            }
            parent = parent.getParent();
        }
        return enabled;
    }

    /**
     * Getting the copy or link status of the specified reference. If the reference is of type
     * {@link IProductCmptTypeAssociationReference} the linked status is always
     * {@link CopyOrLink#UNDEFINED}.
     *
     * @param reference the reference you want to get the linked status for
     * @return true if the reference should be linked, false to copy the reference
     */
    public CopyOrLink getCopyOrLink(IProductCmptStructureReference reference) {
        if (reference instanceof IProductCmptTypeAssociationReference) {
            return CopyOrLink.UNDEFINED;
        }
        if (reference.getWrapped() == null) {
            // looks like the root node.
            return CopyOrLink.COPY;
        }
        return getCopyOrLinkStatus(reference);
    }

    /**
     * Getting all {@link IProductCmptStructureReference} of the given structure that should be
     * copied or linked. By the boolean includeAssociations you could specify whether to check the
     * status of associations (in contrary to compositions/aggregations) or not.
     *
     * @param copyOrLink Whether to get only references that are marked as copied or linked
     * @param structure the structure to get the references from
     * @param includeAssociations whether to include associations or only include
     *            compositions/aggregations
     *
     * @return a set of {@link IProductCmptReference}s that are marked to be copied or linked
     */
    public Set<IProductCmptStructureReference> getAllEnabledElements(CopyOrLink copyOrLink,
            IProductCmptTreeStructure structure,
            boolean includeAssociations) {
        Set<IProductCmptStructureReference> result = new IdentityHashSet<>();
        Set<IProductCmptStructureReference> set = structure.toSet(false);
        for (IProductCmptStructureReference reference : set) {
            if ((reference instanceof IProductCmptTypeAssociationReference)
                    || (!includeAssociations && reference instanceof IProductCmptReference
                            && associationLinks.containsKey(reference.getWrapped()))) {
                continue;
            }
            LinkStatus status = getStatus(reference);
            if (isEnabled(reference) && status.getCopyOrLink() == copyOrLink) {
                result.add(reference);
            }
        }
        return result;
    }

    public Set<IProductCmptStructureReference> getAllElements(CopyOrLink copyOrLink,
            IProductCmptTreeStructure structure,
            boolean includeAssociations) {
        Set<IProductCmptStructureReference> result = new IdentityHashSet<>();
        Set<IProductCmptStructureReference> set = structure.toSet(false);
        for (IProductCmptStructureReference reference : set) {
            if ((reference instanceof IProductCmptTypeAssociationReference)
                    || (!includeAssociations && reference instanceof IProductCmptReference
                            && associationLinks.containsKey(reference.getWrapped()))) {
                continue;
            }
            LinkStatus status = getStatus(reference);
            if (status.getCopyOrLink() == copyOrLink) {
                result.add(reference);
            }
        }
        return result;
    }
}
