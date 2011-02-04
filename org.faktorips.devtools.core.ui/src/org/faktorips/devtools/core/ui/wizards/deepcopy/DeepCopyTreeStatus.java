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

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
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
public class DeepCopyTreeStatus {

    private Map<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> treeStatus = new HashMap<IProductCmpt, Map<IIpsObjectPart, LinkStatus>>();

    public boolean isCheckedStatus(IIpsObjectPart part) {
        return getStatus(part).isChecked();
    }

    public CopyOrLink getCopyOrLinkStatus(IIpsObjectPart part) {
        return getStatus(part).getCopyOrLink();
    }

    /**
     * Getting the status for a {@link IProductCmptLink}. If there is no such status, a new status
     * with default values is created. Never returns null.
     * 
     * @param part the link you want to get the status for
     * @return The status for the link maybe a new one with default values
     */
    public LinkStatus getStatus(IIpsObjectPart part) {
        IProductCmpt parent = getProductCmpt(part);
        Map<IIpsObjectPart, LinkStatus> statusMap = getStatusMap(parent);
        LinkStatus linkStatus = statusMap.get(part);
        if (linkStatus == null) {
            // if there is no status yet, create a new one with default values and return it
            return setStatusInternal(part, null, null);
        }
        return linkStatus;
    }

    private Map<IIpsObjectPart, LinkStatus> getStatusMap(IProductCmpt parent) {
        Map<IIpsObjectPart, LinkStatus> statusMap = treeStatus.get(parent);
        if (statusMap == null) {
            statusMap = new HashMap<IIpsObjectPart, LinkStatus>();
            treeStatus.put(parent, statusMap);
        }
        return statusMap;
    }

    public LinkStatus setStatus(IIpsObjectPart part, CopyOrLink copyOrLink) {
        return setStatusInternal(part, null, copyOrLink);
    }

    public LinkStatus setStatus(IIpsObjectPart part, boolean checked) {
        return setStatusInternal(part, checked, null);
    }

    public LinkStatus setStatus(IIpsObjectPart part, LinkStatus linkStatus) {
        return setStatusInternal(part, linkStatus.isChecked(), linkStatus.getCopyOrLink());
    }

    /**
     * Setting the status for the {@link IProductCmptLink} with the value of checked and copyOrLink.
     * If checked or copyOrLink is null and there is previous value for this status, the old status
     * is preserved. If there was no previous status, the defaults (true, COPY) are set.
     * 
     * @param part The {@link IProductCmptLink} the status should be set for
     * @param checked the checked status or null to preserve the previous value (if any)
     * @param copyOrLink the copy or link status or null to preserve the previous value (if any)
     */
    private LinkStatus setStatusInternal(IIpsObjectPart part, Boolean checked, CopyOrLink copyOrLink) {
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
            linkStatus = new LinkStatus(checked, copyOrLink);
        }
        if (checked != null) {
            linkStatus.setChecked(checked);
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
            if (reference.getWrapped() != null) {
                IIpsObjectPart part = (IIpsObjectPart)reference.getWrapped();
                return isCheckedStatus(part);
            } else {
                // cmptReference.getWrapped() is null --> Root node
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
            // use != LINK because associations are UNDEFINED
            enabled = enabled && isChecked(parent) && getCopyOrLink(parent) != CopyOrLink.LINK;
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
        return getStatus((IIpsObjectPart)reference.getWrapped()).getCopyOrLink();
    }

    public synchronized void addAllPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        for (Map<IIpsObjectPart, LinkStatus> linkStatusMap : treeStatus.values()) {
            for (LinkStatus linkStatus : linkStatusMap.values()) {
                linkStatus.addPropertyChangeListener(propertyChangeListener);
            }
        }
    }

}
