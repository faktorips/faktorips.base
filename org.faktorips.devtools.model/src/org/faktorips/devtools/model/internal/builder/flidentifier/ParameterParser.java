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

import java.text.MessageFormat;
import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IMultiLanguageSupport;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.method.IFormulaMethod;
import org.faktorips.devtools.model.method.IParameter;

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
    public List<IdentifierProposal> getProposals(String prefix) {
        IdentifierProposalCollector collector = new IdentifierProposalCollector();
        if (isContextTypeFormulaType()) {
            IParameter[] parameters = getParameters();
            for (IParameter parameter : parameters) {
                collector.addMatchingNode(parameter.getName(), getDescription(parameter), prefix,
                        IdentifierNodeType.PARAMETER);
            }
        }
        return collector.getProposals();
    }

    String getDescription(IParameter parameter) {
        Datatype datatype = parameter.findDatatype(getIpsProject());
        if (datatype instanceof IIpsElement) {
            IMultiLanguageSupport multiLanguageSupport = getParsingContext().getMultiLanguageSupport();
            return getNameAndDescription((IIpsElement)datatype, multiLanguageSupport);
        } else {
            return MessageFormat.format(Messages.ParameterParser_description, parameter.getName(),
                    parameter.getDatatype());
        }
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