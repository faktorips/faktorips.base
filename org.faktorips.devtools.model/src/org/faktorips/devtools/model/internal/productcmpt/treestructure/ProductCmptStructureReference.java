/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.treestructure;

import java.util.ArrayList;

import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * Abstract reference for <code>ProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public abstract class ProductCmptStructureReference extends PlatformObject implements IProductCmptStructureReference {

    private final IProductCmptTreeStructure structure;

    private final ProductCmptStructureReference parent;

    private ProductCmptStructureReference[] children;

    public ProductCmptStructureReference(IProductCmptTreeStructure structure, ProductCmptStructureReference parent)
            throws CycleInProductStructureException {

        this.structure = structure;
        this.parent = parent;
        children = new ProductCmptStructureReference[0];
        detectCycle(new ArrayList<IIpsElement>());
    }

    @Override
    public IProductCmptTreeStructure getStructure() {
        return structure;
    }

    @Override
    public IProductCmptStructureReference getParent() {
        return parent;
    }

    @Override
    public boolean isRoot() {
        return getParent() == null;
    }

    @Override
    public ProductCmptStructureReference[] getChildren() {
        return children;
    }

    void setChildren(ProductCmptStructureReference[] children) {
        this.children = children;
    }

    private void detectCycle(ArrayList<IIpsElement> seenElements) throws CycleInProductStructureException {
        if (getWrappedIpsObject() instanceof IProductCmpt) {
            if (seenElements.contains(getWrappedIpsObject())) {
                throw new CycleInProductStructureException(seenElements.toArray(new IIpsElement[seenElements.size()]));
            } else {
                seenElements.add(getWrappedIpsObject());
            }
        }
        if (parent != null) {
            parent.detectCycle(seenElements);
        }
    }

    @Override
    public IIpsSrcFile getWrappedIpsSrcFile() {
        if (getWrappedIpsObject() != null) {
            return getWrappedIpsObject().getIpsSrcFile();
        } else {
            return null;
        }
    }

    @Override
    public IIpsProject getIpsProject() {
        if (getWrappedIpsSrcFile() != null) {
            return getWrappedIpsSrcFile().getIpsProject();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ProductCmptStructureReference [ipsObject=" + getWrappedIpsObject() + "," + "wrappedPart=" + getWrapped() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "]"; //$NON-NLS-1$
    }

    /**
     * /** {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProductCmptStructureReference other = (ProductCmptStructureReference)obj;
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        if (structure == null) {
            if (other.structure != null) {
                return false;
            }
        } else if (!structure.equals(other.structure)) {
            return false;
        }
        if (getWrapped() == null) {
            if (other.getWrapped() != null) {
                return false;
            }
        } else if (!getWrapped().equals(other.getWrapped())) {
            return false;
        }
        if (getWrappedIpsObject() == null) {
            if (other.getWrappedIpsObject() != null) {
                return false;
            }
        } else if (!getWrappedIpsObject().equals(other.getWrappedIpsObject())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        if (getWrapped() != null) {
            hash = 31 * hash + getWrapped().hashCode();
        }
        if (getWrappedIpsObject() != null) {
            hash = 31 * hash + getWrappedIpsObject().hashCode();
        }
        if (getStructure() != null) {
            hash = 31 * hash + getStructure().hashCode();
        }
        /*
         * Hash of parent is ignored because its calculation is too inefficient. It would calculate
         * the hash code of all parents recursively. Same parent is not very often case.
         */
        return hash;
    }

}
