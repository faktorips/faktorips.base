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
import org.faktorips.devtools.stdbuilder.xpand.AbstractTypeDeclClassAnnGen;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.XType;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptTypeGen;

public class ProductCmptDeclClassAnnGen extends AbstractTypeDeclClassAnnGen {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {

        JavaCodeFragment annotation = super.createAnnotation(modelNode);

        XProductCmptClass prod = (XProductCmptClass)modelNode;
        annotation.append(createAnnConfigures(prod)).append(createAnnProductCmptTypeGen(prod));

        return annotation;
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XProductCmptClass;
    }

    @Override
    protected JavaCodeFragment createAnnType(XType type) {
        XProductCmptClass prod = (XProductCmptClass)type;

        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        String ipsObjectName = prod.getIpsObjectPartContainer().getQualifiedName();
        String nameParam = "name = \"" + ipsObjectName + "\"";
        String changingOverTimeParam = "changingOverTime = " + prod.isChangingOverTime();
        codeFragmentBuilder.annotationLn(IpsProductCmptType.class,
                StringUtils.join(new String[] { nameParam, changingOverTimeParam }, ", "));
        return codeFragmentBuilder.getFragment();
    }

    /**
     * @return an annotation that annotates the declaration class of the generation of this product
     * @see IpsProductCmptTypeGen
     */
    protected JavaCodeFragment createAnnConfigures(XProductCmptClass prod) {
        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        if (prod.isConfigurationForPolicyCmptType()) {
            codeFragmentBuilder.annotationLn(IpsConfigures.class, prod.getPolicyInterfaceName() + ".class");
            return codeFragmentBuilder.getFragment();
        } else {
            return new JavaCodeFragment();
        }
    }

    /**
     * @return an annotation that annotates which policy this product configures
     * @see IpsConfigures
     */
    protected JavaCodeFragment createAnnProductCmptTypeGen(XProductCmptClass prod) {
        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        if (prod.isChangingOverTime()) {
            codeFragmentBuilder.annotationLn(IpsProductCmptTypeGen.class, prod.getProductCmptGenerationNode()
                    .getImplClassName() + ".class");
            return codeFragmentBuilder.getFragment();
        } else {
            return new JavaCodeFragment();
        }
    }

}
