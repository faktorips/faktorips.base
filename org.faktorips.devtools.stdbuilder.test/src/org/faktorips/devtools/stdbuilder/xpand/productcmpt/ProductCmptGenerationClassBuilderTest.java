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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.net.URL;

import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptClassBuilder;
import org.junit.Test;

public class ProductCmptGenerationClassBuilderTest {

    @Test
    public void testGetTemplate_exists() throws Exception {
        PolicyCmptClassBuilder policyCmptClassBuilder = new PolicyCmptClassBuilder(false,
                mock(StandardBuilderSet.class), mock(GeneratorModelContext.class), null);
        String template = policyCmptClassBuilder.getTemplate();
        int lastIndexOf = template.lastIndexOf(SyntaxConstants.NS_DELIM);
        template = template.substring(0, lastIndexOf);
        String templatePath = template.replaceAll(SyntaxConstants.NS_DELIM, "/") + ".xpt";
        URL resource = PolicyCmptClassBuilder.class.getClassLoader().getResource(templatePath);
        assertNotNull(resource);
    }

}
