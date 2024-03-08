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

import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.model.builder.xmodel.XAssociation;
import org.faktorips.devtools.model.builder.xmodel.XAttribute;
import org.faktorips.devtools.model.builder.xmodel.XType;
import org.faktorips.devtools.model.builder.xmodel.policycmptbuilder.XPolicyBuilder;
import org.faktorips.devtools.model.builder.xmodel.productcmptbuilder.XProductBuilder;

/**
 * Common interface for {@link XPolicyBuilder} and {@link XProductBuilder}.
 */
public interface XPBuilder<B extends XPBuilder<B, AS, AT>, AS extends XAssociation, AT extends XAttribute> {

    /**
     * @see XType#hasSupertype()
     */
    boolean hasSupertype();

    /**
     * @see XType#getSupertype()
     */
    B getSupertype();

    /**
     * @see XType#getAttributes()
     */
    Set<AT> getAttributes();

    Set<AS> getBuilderAssociations();

    boolean hasSuperAssociationBuilder();

    B getSuperBuilderForAssociationBuilder();

    /**
     * @return attributes of the super type that are not overwritten
     */
    Set<AT> getSuperAttributes();

    Map<String, AS> getSuperBuilderAssociationsAsMap();

    boolean isGeneratePublishedInterfaces();

    String getTypeImplClassName();

    String getVariableName();

}
