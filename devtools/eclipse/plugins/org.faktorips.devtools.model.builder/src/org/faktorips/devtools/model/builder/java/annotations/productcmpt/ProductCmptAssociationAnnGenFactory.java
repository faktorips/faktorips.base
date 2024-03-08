/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.productcmpt;

import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGeneratorFactory;
import org.faktorips.devtools.model.builder.java.annotations.association.AssociationWithCardinalityAnnGen;
import org.faktorips.devtools.model.builder.java.annotations.association.SimpleAssociationAnnGen;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationLinks;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;

public class ProductCmptAssociationAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        return switch (type) {
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER -> new ProductCmptAssociationAnnGen();
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_LINKS -> new SimpleAssociationAnnGen(XProductAssociation.class,
                    IpsAssociationLinks.class);
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_SETTER_ADDER -> new SimpleAssociationAnnGen(
                    XProductAssociation.class, IpsAssociationAdder.class);
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_WITH_CARDINALITY_SETTER_ADDER -> new AssociationWithCardinalityAnnGen(
                    XProductAssociation.class, IpsAssociationAdder.class);
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_REMOVER -> new SimpleAssociationAnnGen(XProductAssociation.class,
                    IpsAssociationRemover.class);
            default -> null;
        };
    }

}
