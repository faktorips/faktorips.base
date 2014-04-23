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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.util.ArgumentCheck;

/**
 * A {@link IContentProposalProvider} for {@link IExpression}s for use in a
 * {@link ContentProposalAdapter}.
 */
public class ExpressionProposalProvider implements IContentProposalProvider {

    private final IdentifierParser identifierParser;
    private final IExpression expression;
    private final MultiLanguageSupport multiLanguageSupport;

    public ExpressionProposalProvider(IExpression expression) {
        super();
        ArgumentCheck.notNull(expression);
        this.expression = expression;
        identifierParser = new IdentifierParser(expression, expression.getIpsProject());
        multiLanguageSupport = IpsPlugin.getMultiLanguageSupport();
    }

    public ExpressionProposalProvider(IExpression expression, IdentifierParser identifierParser) {
        super();
        ArgumentCheck.notNull(expression);
        this.expression = expression;
        this.identifierParser = identifierParser;
        multiLanguageSupport = IpsPlugin.getMultiLanguageSupport();
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        List<IContentProposal> result = new LinkedList<IContentProposal>();
        addIdentifierNodes(contents, result);
        addMatchingFunctions(result, contents);
        return result.toArray(new IContentProposal[result.size()]);
    }

    private void addIdentifierNodes(String contents, List<IContentProposal> result) {
        List<IdentifierNode> proposals = identifierParser.getProposals(contents);
        for (IdentifierNode identifierNode : proposals) {
            result.add(new ContentProposal(identifierNode.getText(), identifierNode.getText(), identifierNode
                    .getDescription(multiLanguageSupport)));
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
}
