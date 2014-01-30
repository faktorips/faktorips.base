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

package org.faktorips.devtools.core.builder.flidentifier;

import static org.mockito.Mockito.when;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.mockito.Mock;

public class AbstractParserTest {

    @Mock
    private IExpression expression;
    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IProductCmptType productCmptType;

    public AbstractParserTest() {
        super();
    }

    @Before
    public void mockExpression() throws Exception {
        when(getExpression().findProductCmptType(getIpsProject())).thenReturn(getProductCmptType());
    }

    public IExpression getExpression() {
        return expression;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public IProductCmptType getProductCmptType() {
        return productCmptType;
    }

    public class TestNode extends IdentifierNode {

        TestNode(Datatype datatype) {
            super(datatype, null);
        }

        TestNode(Datatype datatype, boolean listOfTypes) {
            super(datatype, listOfTypes, null);
        }

    }

}