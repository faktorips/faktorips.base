/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.enumtype;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.IAnnotationGeneratorFactory;

public class EnumAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        return switch (type) {
            case ENUM_CLASS -> new EnumDeclClassAnnGen();
            case ENUM_ATTRIBUTE_GETTER -> new EnumAttributeAnnGen();
            default -> null;
        };
    }

}
