/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.ArgumentCheck;

public class DependencyDetail implements IDependencyDetail {

    private IIpsObjectPartContainer part;
    private String propertyName;

    /**
     * Creates a new Instance. Parameters must not be <code>null</code>.
     * 
     * @param part The part of the source causing the dependency.
     * @param propertyName The name of the property causing this dependency.
     */
    public DependencyDetail(IIpsObjectPartContainer part, String propertyName) {
        ArgumentCheck.notNull(part, "The part of a DependencyDetail must not be null"); //$NON-NLS-1$
        ArgumentCheck.notNull(propertyName, "The propertyName of a DependencyDetail must not be null"); //$NON-NLS-1$

        this.part = part;
        this.propertyName = propertyName;
    }

    @Override
    public IIpsObjectPartContainer getPart() {
        return part;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((part == null) ? 0 : part.hashCode());
        result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
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
        if (!(obj instanceof DependencyDetail)) {
            return false;
        }
        DependencyDetail other = (DependencyDetail)obj;
        if (part == null) {
            if (other.part != null) {
                return false;
            }
        } else if (!part.equals(other.part)) {
            return false;
        }
        if (propertyName == null) {
            if (other.propertyName != null) {
                return false;
            }
        } else if (!propertyName.equals(other.propertyName)) {
            return false;
        }
        return true;
    }

}
