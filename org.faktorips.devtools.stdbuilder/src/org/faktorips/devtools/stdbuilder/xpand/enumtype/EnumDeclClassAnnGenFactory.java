/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.enumtype;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGeneratorFactory;

public class EnumDeclClassAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public EnumDeclClassAnnGen createAnnotationGenerator(AnnotatedJavaElementType type) {
        if (type == AnnotatedJavaElementType.ENUM_CLASS) {
            return new EnumDeclClassAnnGen();
        } else {
            return null;
        }
    }

}
