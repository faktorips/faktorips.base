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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.settings.JaxbSupportVariant;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType;
import org.junit.Test;

public class EnumDeclClassJaxbAnnGenTest extends AbstractStdBuilderTest {

    @Test
    public void testIsGenerateAnnotationFor() {
        EnumDeclClassJaxbAnnGen enumDeclClassJaxbAnnGen = new EnumDeclClassJaxbAnnGen();
        String qualifiedName = "foo.Enum";
        IEnumType enumType = newEnumType(ipsProject, qualifiedName);

        XEnumType xEnumType = builderSet.getModelNode(enumType, XEnumType.class);

        assertThat(enumDeclClassJaxbAnnGen.isGenerateAnnotationFor(xEnumType), is(false));

        enumType.setExtensible(true);
        enumType.setAbstract(true);

        assertThat(enumDeclClassJaxbAnnGen.isGenerateAnnotationFor(xEnumType), is(false));

        enumType.setAbstract(false);
        assertThat(enumDeclClassJaxbAnnGen.isGenerateAnnotationFor(xEnumType), is(true));
    }

    @Test
    public void testCreateAnnotation_javax() {
        configureBuilderSetToGenerateJaxbSupport(JaxbSupportVariant.ClassicJAXB);

        EnumDeclClassJaxbAnnGen enumDeclClassJaxbAnnGen = new EnumDeclClassJaxbAnnGen();
        String qualifiedName = "foo.Enum";
        IEnumType enumType = newEnumType(ipsProject, qualifiedName);
        enumType.setExtensible(true);
        XEnumType xEnumType = builderSet.getModelNode(enumType, XEnumType.class);

        JavaCodeFragment fragment = enumDeclClassJaxbAnnGen.createAnnotation(xEnumType);
        assertThat(fragment.getSourcecode(),
                is(equalTo("@XmlJavaTypeAdapter(EnumXmlAdapter.class)" + System.lineSeparator())));
        assertThat(fragment.getImportDeclaration().getImports(),
                hasItem("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter"));
    }

    @Test
    public void testCreateAnnotation_jakarta() {
        configureBuilderSetToGenerateJaxbSupport(JaxbSupportVariant.JakartaXmlBinding3);

        EnumDeclClassJaxbAnnGen enumDeclClassJaxbAnnGen = new EnumDeclClassJaxbAnnGen();
        String qualifiedName = "foo.Enum";
        IEnumType enumType = newEnumType(ipsProject, qualifiedName);
        enumType.setExtensible(true);

        XEnumType xEnumType = builderSet.getModelNode(enumType, XEnumType.class);
        JavaCodeFragment fragment = enumDeclClassJaxbAnnGen.createAnnotation(xEnumType);
        assertThat(fragment.getSourcecode(),
                is(equalTo("@XmlJavaTypeAdapter(EnumXmlAdapter.class)" + System.lineSeparator())));
        assertThat(fragment.getImportDeclaration().getImports(),
                hasItem("jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter"));
    }

    private void configureBuilderSetToGenerateJaxbSupport(JaxbSupportVariant jaxbSupport) {
        IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel configModel = ipsProjectProperties.getBuilderSetConfig();
        configModel.setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT,
                jaxbSupport.name(), null);
        ipsProjectProperties.setBuilderSetConfig(configModel);
        ipsProject.setProperties(ipsProjectProperties);
    }
}
