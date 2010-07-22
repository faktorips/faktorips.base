/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.bf;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;

public class BusinessFunctionBuilderTest extends AbstractStdBuilderTest {

    private final static String BUSINESS_FUNCTION_NAME = "TestBusinessFunction";

    private BusinessFunctionBuilder builder;

    private IBusinessFunction businessFunction;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        builder = new BusinessFunctionBuilder(builderSet, DefaultBuilderSet.KIND_BUSINESS_FUNCTION);
        businessFunction = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                BUSINESS_FUNCTION_NAME);
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaType(businessFunction, false, false, BUSINESS_FUNCTION_NAME);
    }

    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(businessFunction);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass()));
    }

}
