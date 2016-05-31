/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptTypeGen;

public class ProductCmptDeclClassAnnotationGenerator implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XProductCmptClass prod = (XProductCmptClass)modelNode;

        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        String ipsObjectName = prod.getIpsObjectPartContainer().getQualifiedName();
        String nameParam = "name = \"" + ipsObjectName + "\"";
        String changingOverTimeParam = "changingOverTime = " + prod.isChangingOverTime();
        codeFragmentBuilder.annotationLn(IpsProductCmptType.class,
                StringUtils.join(new String[] { nameParam, changingOverTimeParam }, ", "));

        if (prod.isChangingOverTime()) {
            codeFragmentBuilder.annotationLn(IpsProductCmptTypeGen.class, prod.getProductCmptGenerationNode()
                    .getImplClassName() + ".class");
        }

        if (prod.isConfigurationForPolicyCmptType()) {
            codeFragmentBuilder.annotationLn(IpsConfigures.class, prod.getPolicyInterfaceName() + ".class");
        }
        return codeFragmentBuilder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XProductCmptClass;
    }

}
