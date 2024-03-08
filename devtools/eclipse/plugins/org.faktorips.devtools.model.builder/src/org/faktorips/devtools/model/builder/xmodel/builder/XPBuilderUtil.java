/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.model.builder.xmodel.XAssociation;
import org.faktorips.devtools.model.builder.xmodel.XAttribute;

/**
 * Implementation of common methods for {@link XPBuilder}.
 */
public class XPBuilderUtil<B extends XPBuilder<B, AS, AT>, AS extends XAssociation, AT extends XAttribute> {

    private final B builder;

    public XPBuilderUtil(B builder) {
        this.builder = builder;
    }

    public B getSuperBuilderForAssociationBuilder() {
        if (builder.hasSupertype()) {
            B supertype = builder.getSupertype();
            if (supertype.getBuilderAssociations().size() > 0) {
                return supertype;
            } else {
                return supertype.getSuperBuilderForAssociationBuilder();
            }
        } else {
            return null;
        }
    }

    public boolean hasSuperAssociationBuilder() {
        return getSuperBuilderForAssociationBuilder() != null;
    }

    public Map<String, AS> getSuperBuilderAssociationsAsMap() {
        HashMap<String, AS> superAssociations = new HashMap<>();

        if (builder.hasSupertype()) {
            B supertype = builder.getSupertype();
            Map<? extends String, ? extends AS> superBuilderAssociationsAsMap = supertype
                    .getSuperBuilderAssociationsAsMap();
            superAssociations.putAll(superBuilderAssociationsAsMap);
            for (AS association : supertype.getBuilderAssociations()) {
                superAssociations.put(association.getName(), association);
            }
        }

        for (AS ownAssociation : builder.getBuilderAssociations()) {
            superAssociations.remove(ownAssociation.getName());
        }

        return superAssociations;
    }

    public Set<AT> getSuperAttributes() {
        Set<AT> superAttributes = new HashSet<>();
        if (!builder.hasSupertype()) {
            return superAttributes;
        }
        Set<AT> overwrittenAttributes = new HashSet<>();
        for (AT attribute : builder.getAttributes()) {
            if (attribute.isOverwrite()) {
                @SuppressWarnings("unchecked")
                AT overwrittenAttribute = (AT)attribute.getOverwrittenAttribute();
                overwrittenAttributes.add(overwrittenAttribute);
            }
        }
        superAttributes = builder.getSupertype().getAttributes();
        superAttributes.addAll(builder.getSupertype().getSuperAttributes());
        superAttributes.removeAll(overwrittenAttributes);
        return superAttributes;
    }
}
