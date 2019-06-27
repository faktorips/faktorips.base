/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.IAnnotationGeneratorFactory;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xtend.association.SimpleAssociationAnnGen;
import org.faktorips.runtime.model.annotation.IpsAssociationLinks;

public class ProductCmptAssociationAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        switch (type) {
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER:
                return new ProductCmptAssociationAnnGen();
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_LINKS:
                return new SimpleAssociationAnnGen(XProductAssociation.class, IpsAssociationLinks.class);

            default:
                return null;
        }
    }

}
