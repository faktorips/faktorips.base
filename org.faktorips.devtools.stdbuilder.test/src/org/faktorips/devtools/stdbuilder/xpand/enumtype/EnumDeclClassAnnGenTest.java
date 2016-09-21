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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.enumtype.model.XEnumType;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
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
                        + System.getProperty("line.separator"))));
    }

    @Test
    public void testCreateAnnotation() throws CoreException {
        EnumType enumType = setUpEnumtype();
        enumType.newEnumLiteralNameAttribute();
        XEnumType xEnumtype = modelService.getModelNode(enumType, XEnumType.class, modelContext);

        JavaCodeFragment annotation = enumDeclClassAnnGen.createAnnotation(xEnumtype);

        assertThat(
                annotation.getSourcecode(),
                is(equalTo("@IpsEnumType(name = \"test.EnumType\", attributeNames = {\"A2\", \"A1\", \"A3\"})"
                        + System.getProperty("line.separator"))));
    }

    @Test
    public void testCreateAnnotationExtensible() throws CoreException {
        EnumType enumType = setUpEnumtype();
        enumType.newEnumLiteralNameAttribute();
        enumType.setExtensible(true);
        enumType.setEnumContentName("foo.EnumName");
        XEnumType xEnumtype = modelService.getModelNode(enumType, XEnumType.class, modelContext);

        JavaCodeFragment annotation = enumDeclClassAnnGen.createAnnotation(xEnumtype);

        assertThat(
                annotation.getSourcecode(),
                is(equalTo("@IpsEnumType(name = \"test.EnumType\", attributeNames = {\"A2\", \"A1\", \"A3\"})"
                        + System.getProperty("line.separator") + "@IpsExtensibleEnum(enumContentName=\"foo.EnumName\")"
                        + System.getProperty("line.separator"))));
    }

    @Test
    public void testCreateAnnotation_abstractEnumWithAbstractParent() throws CoreException {
        EnumType enumType = setUpEnumtype();
        enumType.setAbstract(true);
        EnumType parentEnumType = newEnumType(ipsProject, "test.AbstractEnumType");
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
                is(equalTo("@IpsEnumType(name = \"test.EnumType\", attributeNames = {\"superAttr1\", \"superAttr2\", \"A2\", \"A1\", \"A3\"})"
                        + System.getProperty("line.separator"))));
    }

    private EnumType setUpEnumtype() throws CoreException {
        EnumType enumType = newEnumType(ipsProject, "test.EnumType");
        IEnumAttribute attribute2 = enumType.newEnumAttribute();
        attribute2.setName("A2");
        IEnumAttribute attribute1 = enumType.newEnumAttribute();
        attribute1.setName("A1");
        IEnumAttribute attribute3 = enumType.newEnumAttribute();
        attribute3.setName("A3");
        return enumType;
    }

}