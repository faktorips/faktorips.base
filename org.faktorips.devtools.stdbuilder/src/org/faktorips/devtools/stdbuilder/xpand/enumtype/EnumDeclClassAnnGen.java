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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xpand.enumtype.model.XEnumAttribute;
import org.faktorips.devtools.stdbuilder.xpand.enumtype.model.XEnumType;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.runtime.model.annotation.IpsEnum;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;

public class EnumDeclClassAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XEnumType enumtype = (XEnumType)modelNode;
        JavaCodeFragment annotation = createIpsEnumAnnotation(enumtype);
        annotation.append(createIpsExtensibleEnumAnnotation(enumtype));
        return annotation;
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XEnumType;
    }

    public JavaCodeFragment createIpsEnumAnnotation(XEnumType enumtype) {
        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        String name = enumtype.getQualifiedIpsObjectName();
        List<XEnumAttribute> attributes = enumtype.getAllAttributes();
        List<String> attributeNames = new ArrayList<String>(attributes.size());
        for (XEnumAttribute attribute : attributes) {
            attributeNames.add(attribute.getName());
        }
        codeFragmentBuilder.annotationLn(IpsEnum.class,
                "name = \"" + name + "\" attributeNames = {\"" + StringUtils.join(attributeNames, "\", \"") + "\"}");
        return codeFragmentBuilder.getFragment();
    }

    public JavaCodeFragment createIpsExtensibleEnumAnnotation(XEnumType enumtype) {
        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();
        if (enumtype.isExtensible()) {
            String enumContentName = enumtype.getEnumContentQualifiedName();
            codeFragmentBuilder.annotationLn(IpsExtensibleEnum.class, "enumContentName", enumContentName);
        }
        return codeFragmentBuilder.getFragment();
    }

}
