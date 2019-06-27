/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.enumtype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType;
import org.faktorips.devtools.stdbuilder.xtend.enumtype.EnumDeclClassJaxbAnnGen;
import org.junit.Test;

public class EnumDeclClassJaxbAnnGenTest extends AbstractStdBuilderTest {

    @Test
    public void testIsGenerateAnnotationFor() throws CoreException {
        EnumDeclClassJaxbAnnGen enumDeclClassJaxbAnnGen = new EnumDeclClassJaxbAnnGen();
        String qualifiedName = "foo.Enum";
        EnumType enumType = newEnumType(ipsProject, qualifiedName);

        XEnumType xEnumType = builderSet.getModelNode(enumType, XEnumType.class);

        assertThat(enumDeclClassJaxbAnnGen.isGenerateAnnotationFor(xEnumType), is(false));

        enumType.setExtensible(true);
        enumType.setAbstract(true);

        assertThat(enumDeclClassJaxbAnnGen.isGenerateAnnotationFor(xEnumType), is(false));

        enumType.setAbstract(false);
        assertThat(enumDeclClassJaxbAnnGen.isGenerateAnnotationFor(xEnumType), is(true));
    }

    @Test
    public void testCreateAnnotation() throws CoreException {
        EnumDeclClassJaxbAnnGen enumDeclClassJaxbAnnGen = new EnumDeclClassJaxbAnnGen();
        String qualifiedName = "foo.Enum";
        EnumType enumType = newEnumType(ipsProject, qualifiedName);
        enumType.setExtensible(true);

        XEnumType xEnumType = builderSet.getModelNode(enumType, XEnumType.class);

        assertThat(enumDeclClassJaxbAnnGen.createAnnotation(xEnumType).getSourcecode(),
                is(equalTo("@XmlJavaTypeAdapter(EnumXmlAdapter.class)" + System.getProperty("line.separator"))));
    }

}
