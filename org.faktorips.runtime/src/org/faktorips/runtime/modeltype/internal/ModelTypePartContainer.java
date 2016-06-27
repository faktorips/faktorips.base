/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.ITableUsageModel;

public class ModelTypePartContainer {
    private final LinkedHashMap<String, IModelTypeAttribute> attributes;

    private final LinkedHashMap<String, IModelTypeAssociation> associations;

    private final LinkedHashMap<String, ITableUsageModel> tableUsages;

    public ModelTypePartContainer(LinkedHashMap<String, IModelTypeAttribute> attributes,
            LinkedHashMap<String, IModelTypeAssociation> associations,
            LinkedHashMap<String, ITableUsageModel> tableUsages) {
        this.attributes = attributes;
        this.associations = associations;
        this.tableUsages = tableUsages;
    }

    public Collection<IModelTypeAttribute> getAttributes() {
        return attributes.values();
    }

    public Collection<IModelTypeAssociation> getAssociations() {
        return associations.values();
    }

    public Collection<ITableUsageModel> getTableUsages() {
        return tableUsages.values();
    }

    public IModelTypeAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    public IModelTypeAssociation getAssociation(String name) {
        return associations.get(name);
    }

    public ITableUsageModel getTableUsageModel(String name) {
        return tableUsages.get(name);
    }
}
