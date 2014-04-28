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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.method.IParameter;

/**
 * The {@link ParameterParser} parses an identifier part to an {@link ParameterNode}. It checks
 * whether the identifier part matches any parameter of the expression signature. In this case it
 * will return an {@link ParameterNode} otherwise <code>null</code>.
 * 
 * @author dirmeier
 */
public class ParameterParser extends AbstractIdentifierNodeParser {

    public ParameterParser(ParsingContext parsingContext) {
        super(parsingContext);
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

    @Override
    public List<IdentifierNode> getProposals(String prefix) {
        ArrayList<IdentifierNode> result = new ArrayList<IdentifierNode>();
        if (isContextTypeFormulaType()) {
            IParameter[] parameters = getParameters();
            for (IParameter parameter : parameters) {
                if (StringUtils.startsWithIgnoreCase(parameter.getName(), prefix)) {
                    result.add(nodeFactory().createParameterNode(parameter));
                }
            }
        }
        return result;
    }

    /* private */protected IParameter[] getParameters() {
        IFormulaMethod formulaSignature = getExpression().findFormulaSignature(getIpsProject());
        if (formulaSignature != null) {
            return formulaSignature.getParameters();
        } else {
            return new IParameter[0];
        }
    }

}