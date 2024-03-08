/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.enums;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.java.annotations.JaxbAnnGenFactory.JaxbAnnotation;
import org.faktorips.devtools.model.builder.java.naming.EnumXmlAdapterNameProvider;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.naming.JavaClassNaming;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumType;
import org.faktorips.devtools.model.enums.IEnumType;

public class EnumDeclClassJaxbAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XEnumType xEnumType = (XEnumType)modelNode;
        return createXmlJavaTypeAdapterAnnotation(xEnumType);
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        if (!(modelNode instanceof XEnumType xEnumType)) {
            return false;
        } else {
            return (xEnumType.isExtensible() && !xEnumType.isAbstract());
        }
    }

    private JavaCodeFragment createXmlJavaTypeAdapterAnnotation(XEnumType xEnumType) {
        IEnumType enumType = xEnumType.getEnumType();
        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        EnumXmlAdapterNameProvider enumXmlAdapterNameProvider = new EnumXmlAdapterNameProvider(xEnumType.getContext()
                .getGeneratorConfig(enumType).isGeneratePublishedInterfaces(enumType.getIpsProject()));
        JavaClassNaming javaClassNaming = new JavaClassNaming(
                (DefaultBuilderSet)enumType.getIpsProject().getIpsArtefactBuilderSet(), true);
        String qualifiedClassName = javaClassNaming.getQualifiedClassName(enumType.getIpsSrcFile(),
                BuilderAspect.getValue(false), enumXmlAdapterNameProvider);
        codeFragmentBuilder.annotationLn(getXmlJavaTypeAdapter(xEnumType),
                xEnumType.addImport(qualifiedClassName) + ".class");

        return codeFragmentBuilder.getFragment();
    }

    private String getXmlJavaTypeAdapter(AbstractGeneratorModelNode modelNode) {
        return JaxbAnnotation.XmlJavaTypeAdapter.qualifiedNameFrom(modelNode.getGeneratorConfig().getJaxbSupport());
    }

}
