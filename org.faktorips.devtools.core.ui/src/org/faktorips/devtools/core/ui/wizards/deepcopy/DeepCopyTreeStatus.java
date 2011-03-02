/*******************************************************************************
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;

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

    /**
     * Initializes the tree status for all references in the structure with default values
     * 
     * @param structure the structure containing all references a status should be initialized for
     */
    public void initialize(IProductCmptTreeStructure structure) {
        treeStatus = new HashMap<IProductCmpt, Map<IIpsObjectPart, LinkStatus>>();
        associationLinks = new HashMap<IProductCmptLink, IProductCmpt>();
        HashMap<IProductCmptLink, IProductCmpt> associationLinksCopy = new HashMap<IProductCmptLink, IProductCmpt>();
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference instanceof IProductCmptTypeAssociationReference) {
                // no status for associations is needed
                continue;
            } else {
                LinkStatus status = getStatus(reference);
                if (reference instanceof IProductCmptReference) {
                    IProductCmptReference cmptReference = (IProductCmptReference)reference;
                    IProductCmptTypeAssociationReference parent = (IProductCmptTypeAssociationReference)cmptReference
                            .getParent();
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
            return setStatusInternal(reference, null, null);
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
            if (reference instanceof IProductCmptTypeAssociationReference) {
                continue;
            }
            if (reference instanceof IProductCmptReference && associationLinks.containsKey(reference.getWrapped())) {
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
        Map<IIpsObjectPart, LinkStatus> statusMap = treeStatus.get(parent);
        if (statusMap == null) {
            statusMap = new HashMap<IIpsObjectPart, LinkStatus>();
            treeStatus.put(parent, statusMap);
        }
        return statusMap;
    }

    /**
     * Setting the status for the {@link IProductCmptLink} with the value of checked and copyOrLink.
     * If checked or copyOrLink is null and there is previous value for this status, the old status
     * is preserved. If there was no previous status, the defaults (true, COPY) are set.
     * 
     * @param reference The {@link IIpsObjectPart} the status should be set for
     * @param checked the checked status or null to preserve the previous value (if any)
     * @param copyOrLink the copy or link status or null to preserve the previous value (if any)
     */
    private LinkStatus setStatusInternal(IProductCmptStructureReference reference,
            Boolean checked,
            CopyOrLink copyOrLink) {
        IIpsObjectPart part = reference.getWrapped();
        IProductCmpt parent = getProductCmpt(part);
        Map<IIpsObjectPart, LinkStatus> statusMap = getStatusMap(parent);
        LinkStatus linkStatus = statusMap.get(part);
        if (linkStatus == null) {
            if (checked == null) {
                checked = true;
            }
            if (copyOrLink == null) {
                copyOrLink = CopyOrLink.COPY;
            }
            linkStatus = new LinkStatus(reference.getWrapped(), reference.getWrappedIpsObject(), checked, copyOrLink);
        }
        if (checked != null) {
            if (reference.getParent() == null) {
                // root node must be checked
                linkStatus.setChecked(true);
            } else {
                linkStatus.setChecked(checked);
            }
        }
        if (copyOrLink != null) {
            linkStatus.setCopyOrLink(copyOrLink);
        }
        statusMap.put(part, linkStatus);
        return linkStatus;
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
        // both tableUsages and links are part of the generation. So we have to call getParent twice
        IIpsElement parent = partOfProductCmpt.getParent().getParent();
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
            if (reference.getParent() != null) {
                return getStatus(reference).isChecked();
            } else {
                // getParent() is null --> Root node
                return true;
            }
        } else if (reference instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)reference;
            for (IProductCmptReference child : associationReference.getStructure().getChildProductCmptReferences(
                    associationReference)) {
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
        } else if (reference instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)reference;
            oldValue = false;
            for (IProductCmptReference child : associationReference.getStructure().getChildProductCmptReferences(
                    associationReference)) {
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
        setStatusInternal(reference, value, null);
        return oldValue;
    }

    /**
     * Setting the copy or link status for the give {@link IProductCmptStructureReference}. If the
     * reference is a {@link IProductCmptTypeAssociationReference} this method do nothing
     * 
     * @param reference the reference you want to change the status for
     * @param value the new status of the reference
     */
    public void setCopOrLink(IProductCmptStructureReference reference, CopyOrLink value) {
        if (reference instanceof IProductCmptReference || reference instanceof IProductCmptStructureTblUsageReference) {
            LinkStatus status = getStatus(reference);
            CopyOrLink oldValue = status.getCopyOrLink();
            setStatusInternal(reference, null, value);
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
        IProductCmptStructureReference parent = reference;
        while (enabled && (parent = parent.getParent()) != null) {
            if (parent instanceof IProductCmptTypeAssociationReference) {
                continue;
            } else {
                LinkStatus status = getStatus(parent);
                enabled = enabled && status.isChecked() && status.getCopyOrLink() == CopyOrLink.COPY;
            }
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
        HashSet<IProductCmptStructureReference> result = new HashSet<IProductCmptStructureReference>();
        Set<IProductCmptStructureReference> set = structure.toSet(false);
        for (IProductCmptStructureReference reference : set) {
            if (reference instanceof IProductCmptTypeAssociationReference) {
                continue;
            }
            if (!includeAssociations && reference instanceof IProductCmptReference
                    && associationLinks.containsKey(reference.getWrapped())) {
                continue;
            }
            LinkStatus status = getStatus(reference);
            if (isEnabled(reference) && status.getCopyOrLink() == copyOrLink) {
                result.add(reference);
            }
        }
        return result;
    }

}
