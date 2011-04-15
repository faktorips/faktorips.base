/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import java.util.ArrayList;

import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;

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
    public String toString() {
        return "ProductCmptStructureReference [ipsObject=" + getWrappedIpsObject() + "," + "wrappedPart=" + getWrapped() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
        } else if (parent.equals(other.parent)) {
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
