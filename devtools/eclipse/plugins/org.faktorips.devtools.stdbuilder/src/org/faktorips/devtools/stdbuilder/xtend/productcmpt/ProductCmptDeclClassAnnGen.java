/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.XType;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XTableUsage;
import org.faktorips.devtools.stdbuilder.xtend.AbstractTypeDeclClassAnnGen;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsTableUsages;

public class ProductCmptDeclClassAnnGen extends AbstractTypeDeclClassAnnGen {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {

        JavaCodeFragment annotation = super.createAnnotation(modelNode);

        XProductCmptClass prod = (XProductCmptClass)modelNode;
        annotation.append(createAnnConfigures(prod)).append(createAnnProductCmptTypeGen(prod))
                .append(createAnnTableUsages(prod));

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
        codeFragmentBuilder.annotationLn(IpsProductCmptType.class, nameParam);
        return codeFragmentBuilder.getFragment();
    }

    /**
     * @return an annotation that annotates the declaration class of the generation of this product
     * @see IpsChangingOverTime
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
            codeFragmentBuilder.annotationLn(IpsChangingOverTime.class, prod.getProductCmptGenerationNode()
                    .getInterfaceName() + ".class");
            return codeFragmentBuilder.getFragment();
        } else {
            return new JavaCodeFragment();
        }
    }

    /**
     * @return an annotation containing the names of all {@code XTableUsage}s if the type has any.
     * @see IpsTableUsages
     */
    protected JavaCodeFragment createAnnTableUsages(XProductCmptClass prod) {
        Set<XTableUsage> tableUsages = prod.getAllDeclaredTables();
        Class<?> annotationClass = IpsTableUsages.class;
        return createAnnotationWithNodes(annotationClass, tableUsages);
    }
}
