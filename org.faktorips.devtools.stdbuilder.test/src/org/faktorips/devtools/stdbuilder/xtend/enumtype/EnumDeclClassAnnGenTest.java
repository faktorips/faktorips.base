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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.enumtype.XEnumType;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;

public class EnumDeclClassAnnGenTest extends AbstractStdBuilderTest {

    private EnumDeclClassAnnGen enumDeclClassAnnGen;
    private ModelService modelService;
    private GeneratorModelContext modelContext;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        enumDeclClassAnnGen = new EnumDeclClassAnnGen();
        modelService = new ModelService();
        modelContext = builderSet.getGeneratorModelContext();
    }

    @Test
    public void testCreateAnnotationForAbstractEnum() throws CoreException {
        XEnumType xEnumtype = modelService.getModelNode(setUpEnumtype(), XEnumType.class, modelContext);

        JavaCodeFragment annotation = enumDeclClassAnnGen.createAnnotation(xEnumtype);

        assertThat(
                annotation.getSourcecode(),
                is(equalTo("@IpsEnumType(name = \"test.EnumType\", attributeNames = {\"A2\", \"A1\", \"A3\"})"
                        + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation() throws CoreException {
        IEnumType enumType = setUpEnumtype();
        enumType.newEnumLiteralNameAttribute();
        XEnumType xEnumtype = modelService.getModelNode(enumType, XEnumType.class, modelContext);

        JavaCodeFragment annotation = enumDeclClassAnnGen.createAnnotation(xEnumtype);

        assertThat(
                annotation.getSourcecode(),
                is(equalTo("@IpsEnumType(name = \"test.EnumType\", attributeNames = {\"A2\", \"A1\", \"A3\"})"
                        + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotationExtensible() throws CoreException {
        IEnumType enumType = setUpEnumtype();
        enumType.newEnumLiteralNameAttribute();
        enumType.setExtensible(true);
        enumType.setEnumContentName("foo.EnumName");
        XEnumType xEnumtype = modelService.getModelNode(enumType, XEnumType.class, modelContext);

        JavaCodeFragment annotation = enumDeclClassAnnGen.createAnnotation(xEnumtype);

        assertThat(
                annotation.getSourcecode(),
                is(equalTo("@IpsEnumType(name = \"test.EnumType\", attributeNames = {\"A2\", \"A1\", \"A3\"})"
                        + System.lineSeparator() + "@IpsExtensibleEnum(enumContentName=\"foo.EnumName\")"
                        + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_abstractEnumWithAbstractParent() throws CoreException {
        IEnumType enumType = setUpEnumtype();
        enumType.setAbstract(true);
        IEnumType parentEnumType = newEnumType(ipsProject, "test.AbstractEnumType");
        parentEnumType.setAbstract(true);
        IEnumAttribute superAttr1 = parentEnumType.newEnumAttribute();
        superAttr1.setName("superAttr1");
        IEnumAttribute superAttr2 = parentEnumType.newEnumAttribute();
        superAttr2.setName("superAttr2");
        enumType.setSuperEnumType("test.AbstractEnumType");
        XEnumType xEnumtype = modelService.getModelNode(enumType, XEnumType.class, modelContext);

        JavaCodeFragment annotation = enumDeclClassAnnGen.createAnnotation(xEnumtype);

        assertThat(
                annotation.getSourcecode(),
                is(equalTo(
                        "@IpsEnumType(name = \"test.EnumType\", attributeNames = {\"superAttr1\", \"superAttr2\", \"A2\", \"A1\", \"A3\"})"
                                + System.lineSeparator())));
    }

    private IEnumType setUpEnumtype() throws CoreException {
        IEnumType enumType = newEnumType(ipsProject, "test.EnumType");
        IEnumAttribute attribute2 = enumType.newEnumAttribute();
        attribute2.setName("A2");
        IEnumAttribute attribute1 = enumType.newEnumAttribute();
        attribute1.setName("A1");
        IEnumAttribute attribute3 = enumType.newEnumAttribute();
        attribute3.setName("A3");
        return enumType;
    }

}