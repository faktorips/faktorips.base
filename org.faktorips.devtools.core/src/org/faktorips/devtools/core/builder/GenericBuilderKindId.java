/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.UUID;

import org.faktorips.devtools.core.model.ipsproject.IBuilderKindId;

public class GenericBuilderKindId implements IBuilderKindId {

    private final String id;

    public GenericBuilderKindId() {
        id = UUID.randomUUID().toString();
    }

    public GenericBuilderKindId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        GenericBuilderKindId other = (GenericBuilderKindId)obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GenericBuilderKindId [id=" + id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}