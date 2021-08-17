/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.attribute;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.IAnnotationGeneratorFactory;

/**
 * Creates Generators for Annotations used on {@linkplain IAttribute attributes}.
 */
public class AttributeAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        switch (type) {
            case POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER:
            case PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_GETTER:
                return new AttributeGetterAnnGen();
            case POLICY_CMPT_DECL_CLASS_ATTRIBUTE_SETTER:
            case PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_SETTER:
                return new AttributeSetterAnnGen();
            case POLICY_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES:
            case PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES:
                return new AttributeAllowedValuesAnnGen();
            case PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT:
                return new AttributeDefaultValueAnnGen();
            case PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT_SETTER:
                return new AttributeDefaultValueSetterAnnGen();
            case PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES_SETTER:
                return new AttributeAllowedValuesSetterAnnGen();
            default:
                return null;
        }
    }
}
