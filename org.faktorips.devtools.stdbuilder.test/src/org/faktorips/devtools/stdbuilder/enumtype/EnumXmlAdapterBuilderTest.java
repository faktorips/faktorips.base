/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class EnumXmlAdapterBuilderTest extends AbstractStdBuilderTest {

    private final static String ENUM_TYPE_NAME = "TestEnumType";

    private EnumXmlAdapterBuilder builder;

    private IEnumType enumType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        EnumTypeBuilder enumTypeBuilder = new EnumTypeBuilder(builderSet);
        builder = new EnumXmlAdapterBuilder(builderSet, enumTypeBuilder);
        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
        enumType.setExtensible(true);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(enumType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaXmlAdapter()));
    }

    private IType getGeneratedJavaXmlAdapter() {
        return getGeneratedJavaClass(enumType, true, ENUM_TYPE_NAME + "XmlAdapter");
    }

}
