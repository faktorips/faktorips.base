/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public abstract class IpsElement extends PlatformObject implements IIpsElement {

    private static final IIpsElement[] NO_CHILDREN = new IIpsElement[0];

    // FIXME make private
    // CSOFF: VisibilityModifierCheck
    protected String name;
    // CSON: VisibilityModifierCheck
    private IIpsElement parent;

    public IpsElement(IIpsElement parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Constructor for testing purposes.
     */
    public IpsElement() {
        // Constructor for testing purposes.
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public final IIpsElement getParent() {
        return parent;
    }

    @Override
    public boolean exists() {
        if (getParent() == null || !getParent().exists()) {
            return false;
        }
        if (getCorrespondingResource() == null) {
            /*
             * If no corresponding resource exists, the EnclosingResource.exists() is handled by
             * calling getParent().exists() above. So if we have arrived here, we have to return
             * true (the parent exists) to avoid a NullPointerException in the rest of the code.
             */
            return true;
        }
        return getCorrespondingResource().exists();
    }

    @Override
    public AResource getEnclosingResource() {
        AResource resource = getCorrespondingResource();
        if (resource != null) {
            return resource;
        }
        return getParent().getEnclosingResource();
    }

    @Override
    public IIpsModel getIpsModel() {
        return IIpsModel.get();
    }

    @Override
    public IIpsProject getIpsProject() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getIpsProject();
    }

    @Override
    public IIpsElement[] getChildren() {
        return NO_CHILDREN;
    }

    @Override
    public boolean hasChildren() {
        return getChildren().length > 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((getParent() == null) ? 0 : getParent().hashCode());
        return result;
    }

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
        IpsElement other = (IpsElement)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (getParent() == null) {
            if (other.getParent() != null) {
                return false;
            }
        } else if (!getParent().equals(other.getParent())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (getParent() == null) {
            return getName();
        }
        return getParent().toString() + "/" + getName(); //$NON-NLS-1$
    }

    @Override
    public boolean isContainedInArchive() {
        return getParent().isContainedInArchive();
    }

}
