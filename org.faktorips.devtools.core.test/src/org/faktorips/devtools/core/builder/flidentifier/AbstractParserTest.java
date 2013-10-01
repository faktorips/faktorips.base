/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
            super(datatype);
        }

        TestNode(Datatype datatype, boolean listOfTypes) {
            super(datatype, listOfTypes);
        }

    }

}