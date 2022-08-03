/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import java.util.Objects;
import java.util.UUID;

import org.faktorips.devtools.model.ipsproject.IBuilderKindId;

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
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        GenericBuilderKindId other = (GenericBuilderKindId)obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "GenericBuilderKindId [id=" + id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
