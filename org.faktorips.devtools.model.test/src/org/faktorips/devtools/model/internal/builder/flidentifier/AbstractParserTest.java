/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import static org.mockito.Mockito.when;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IMultiLanguageSupport;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.mockito.Mock;

public class AbstractParserTest {

    @Mock
    private IFormula expression;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IMultiLanguageSupport multiLanguageSupport;

    private ParsingContext parsingContext;

    public AbstractParserTest() {
        super();
    }

    @Before
    public void mockExpression() throws Exception {
        when(getExpression().findProductCmptType(getIpsProject())).thenReturn(getProductCmptType());
        parsingContext = new ParsingContext(getExpression(), getIpsProject(), getMultiLanguageSupport());
    }

    public ParsingContext getParsingContext() {
        return parsingContext;
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

    public IMultiLanguageSupport getMultiLanguageSupport() {
        return multiLanguageSupport;
    }

    public class TestNode extends IdentifierNode {

        public TestNode(Datatype datatype) {
            super(datatype, null);
        }

        TestNode(Datatype datatype, boolean listOfTypes) {
            super(datatype, listOfTypes, null);
        }

    }

}
