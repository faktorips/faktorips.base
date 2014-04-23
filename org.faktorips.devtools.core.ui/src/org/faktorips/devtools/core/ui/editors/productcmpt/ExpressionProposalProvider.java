/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.runtime.internal.StringUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * A {@link IContentProposalProvider} for {@link IExpression}s for use in a
 * {@link ContentProposalAdapter}.
 * 
 * @author schwering
 */
public class ExpressionProposalProvider implements IContentProposalProvider {

    private IdentifierParser identifierParser;
    private IExpression expression;

    public ExpressionProposalProvider(IExpression expression) {
        super();
        ArgumentCheck.notNull(expression);
        this.expression = expression;
        identifierParser = new IdentifierParser(expression, expression.getIpsProject());
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String identifier = getLastIdentifier(contents.substring(0, position));
        List<IContentProposal> result = new LinkedList<IContentProposal>();
        addIdentifierNodes(identifier, result);
        addMatchingFunctions(result, identifier);
        return result.toArray(new IContentProposal[result.size()]);
    }

    private void addIdentifierNodes(String contents, List<IContentProposal> result) {
        List<IdentifierNode> proposals = identifierParser.getProposals(contents);
        for (IdentifierNode identifierNode : proposals) {
            result.add(new ContentProposal(identifierNode.getText(), identifierNode.getText(), identifierNode
                    .getDescription()));
        }
    }

    private void addMatchingFunctions(List<IContentProposal> result, String prefix) {
        ExprCompiler<JavaCodeFragment> compiler = expression.newExprCompiler(expression.getIpsProject());
        FlFunction<JavaCodeFragment>[] functions = compiler.getFunctions();
        for (FlFunction<JavaCodeFragment> function : functions) {
            if (checkMatchingNameWithCaseInsensitive(function.getName(), prefix)) {
                addFunctionToResult(result, function, prefix);
            }
        }
    }

    protected boolean checkMatchingNameWithCaseInsensitive(final String name, final String prefix) {
        if (name.toLowerCase().startsWith(prefix.toLowerCase())) {
            return true;
        }
        return checkMatchingQualifiedName(name, prefix);
    }

    private boolean checkMatchingQualifiedName(final String name, final String prefix) {
        String newName = name;
        int pos = newName.indexOf('.');
        while (pos > 0) {
            newName = newName.substring(pos + 1);
            if (newName.startsWith(prefix)) {
                return true;
            }
            pos = newName.indexOf('.');
        }
        return false;
    }

    private void addFunctionToResult(List<IContentProposal> result, FlFunction<JavaCodeFragment> function, String prefix) {
        String name = function.getName();
        StringBuffer displayText = new StringBuffer(name);
        displayText.append('(');
        Datatype[] argTypes = function.getArgTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (i > 0) {
                displayText.append("; "); //$NON-NLS-1$
            }
            displayText.append(argTypes[i].getName());
        }
        displayText.append(')');
        displayText.append(" - "); //$NON-NLS-1$
        displayText.append(function.getType().getName());
        String description = function.getDescription();
        ContentProposal proposal = new ContentProposal(name, displayText.toString(), description, prefix);
        result.add(proposal);
    }

    /**
     * The characters that are checked within this method have to be in synch with the identifier
     * tokens defined in the ffl.jjt grammar
     */
    private String getLastIdentifier(String s) {
        if (StringUtils.isEmpty(s)) {
            return ""; //$NON-NLS-1$
        }
        int i = s.length() - 1;
        boolean isInQuotes = false;
        while (i >= 0) {
            char c = s.charAt(i);
            if (c == '"') {
                isInQuotes = !isInQuotes;
            } else if (!isLegalChar(c, isInQuotes)) {
                break;
            }
            i--;
        }
        return s.substring(i + 1);
    }

    private boolean isLegalChar(char c, boolean isInQuotes) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '-' || c == '[' || c == ']'
                || (isInQuotes && c == ' ');
    }

    protected void setIdentifierParser(IdentifierParser identifierParser) {
        this.identifierParser = identifierParser;
    }
}
