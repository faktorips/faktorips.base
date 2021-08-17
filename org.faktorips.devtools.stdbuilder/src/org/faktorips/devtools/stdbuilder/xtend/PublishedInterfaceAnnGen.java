/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.XType;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;

public class PublishedInterfaceAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        return new JavaCodeFragmentBuilder().annotationLn(IpsPublishedInterface.class,
                "implementation = " + ((XType)modelNode).getImplClassName() + ".class").getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XPolicyCmptClass || modelNode instanceof XProductCmptClass
                || modelNode instanceof XProductCmptGenerationClass;
    }
}
