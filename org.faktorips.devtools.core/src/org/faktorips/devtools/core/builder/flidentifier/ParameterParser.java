/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.productcmpt.IExpression;

/**
 * The {@link ParameterParser} parses an identifier part to an {@link ParameterNode}. It checks
 * whether the identifier part matches any parameter of the expression signature. In this case it
 * will return an {@link ParameterNode} otherwise <code>null</code>.
 * 
 * @author dirmeier
 */
public class ParameterParser extends AbstractIdentifierNodeParser {

    public ParameterParser(IExpression expression, IIpsProject ipsProject) {
        super(expression, ipsProject);
    }

    @Override
    protected IdentifierNode parse() {
        if (isContextTypeFormulaType()) {
            IParameter[] params = getParameters();
            for (IParameter param : params) {
                if (param.getName().equals(getIdentifierPart())) {
                    return nodeFactory().createParameterNode(param);
                }
            }
        }
        return null;
    }

    /* private */protected IParameter[] getParameters() {
        return getExpression().findFormulaSignature(getIpsProject()).getParameters();
    }

}