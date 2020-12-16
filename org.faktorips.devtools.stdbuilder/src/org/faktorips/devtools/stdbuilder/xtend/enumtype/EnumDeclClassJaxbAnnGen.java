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

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.BuilderKindIds;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType;

public class EnumDeclClassJaxbAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XEnumType xEnumType = (XEnumType)modelNode;
        return createXmlJavaTypeAdapterAnnotation(xEnumType);
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        if (!(modelNode instanceof XEnumType)) {
            return false;
        } else {
            XEnumType xEnumType = (XEnumType)modelNode;
            return (xEnumType.isExtensible() && !xEnumType.isAbstract());
        }
    }

    private JavaCodeFragment createXmlJavaTypeAdapterAnnotation(XEnumType xEnumType) {
        IEnumType enumType = xEnumType.getEnumType();
        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        EnumXmlAdapterBuilder xmlAdapterBuilder = xEnumType.getIpsProject().getIpsArtefactBuilderSet()
                .getBuilderById(BuilderKindIds.ENUM_XML_ADAPTER, EnumXmlAdapterBuilder.class);
        codeFragmentBuilder.annotationLn(XmlJavaTypeAdapter.class,
                xEnumType.addImport(xmlAdapterBuilder.getQualifiedClassName(enumType)) + ".class");

        return codeFragmentBuilder.getFragment();
    }

}
