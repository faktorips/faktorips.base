/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.association;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.XAssociation;

public class AssociationWithCardinalityAnnGen extends SimpleAssociationAnnGen {

    private Class<?> annotationClass;

    public AssociationWithCardinalityAnnGen(Class<? extends XAssociation> associationClass, Class<?> annotationClass) {
        super(associationClass, annotationClass);
        this.annotationClass = annotationClass;
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        if (!(modelNode instanceof XAssociation)) {
            return new JavaCodeFragment();
        } else {
            XAssociation association = (XAssociation)modelNode;
            return new JavaCodeFragmentBuilder()
                    .annotationLn(annotationClass,
                            "association = \"" + association.getName(false) + "\", withCardinality = true")
                    .getFragment();
        }
    }

}
