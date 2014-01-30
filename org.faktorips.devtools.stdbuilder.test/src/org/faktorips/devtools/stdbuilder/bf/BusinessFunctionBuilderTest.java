/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.bf;

import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class BusinessFunctionBuilderTest extends AbstractStdBuilderTest {

    private final static String BUSINESS_FUNCTION_NAME = "TestBusinessFunction";

    private BusinessFunctionBuilder builder;

    private IBusinessFunction businessFunction;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        builder = new BusinessFunctionBuilder(builderSet);
        businessFunction = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                BUSINESS_FUNCTION_NAME);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(businessFunction);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass()));
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaClass(businessFunction, true, false, BUSINESS_FUNCTION_NAME);
    }

}
