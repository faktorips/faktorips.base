/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand;

import java.util.EnumSet;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.IAnnotationGeneratorFactory;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.runtime.model.annotation.IpsDocumented;

public class LabelAndDescriptionAnnGen implements IAnnotationGenerator {

    private static final EnumSet<AnnotatedJavaElementType> RELEVANT_TYPES = EnumSet.of(
            AnnotatedJavaElementType.TABLE_CLASS, AnnotatedJavaElementType.POLICY_CMPT_DECLARATION_CLASS,
            AnnotatedJavaElementType.PRODUCT_CMPT_DECLARATION_CLASS);

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        String defaultLocaleLanguage = modelNode.getIpsProject().getReadOnlyProperties().getDefaultLanguage()
                .getLocale().getLanguage();
        String bundleBaseName = modelNode.getDocumentationResourceBundleBaseName();

        return new JavaCodeFragmentBuilder().annotationLn(IpsDocumented.class,
                "bundleName = \"" + bundleBaseName + "\", defaultLocale = \"" + defaultLocaleLanguage + "\"")
                .getFragment();

    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode ipsElement) {
        return true;
    }

    public static class Factory implements IAnnotationGeneratorFactory {

        @Override
        public boolean isRequiredFor(IIpsProject ipsProject) {
            return true;
        }

        @Override
        public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
            if (isRelevant(type)) {
                return new LabelAndDescriptionAnnGen();
            } else {
                return null;
            }
        }

        private boolean isRelevant(AnnotatedJavaElementType type) {
            return RELEVANT_TYPES.contains(type);
        }
    }
}