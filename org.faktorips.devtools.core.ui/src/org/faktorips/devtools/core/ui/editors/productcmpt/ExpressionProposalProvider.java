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

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierProposal;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.parser.FlParserTokenManager;
import org.faktorips.fl.parser.JavaCharStream;
import org.faktorips.fl.parser.Token;
import org.faktorips.util.ArgumentCheck;

/**
 * A {@link IContentProposalProvider} for {@link IExpression}s for use in a
 * {@link ContentProposalAdapter}.
 */
public class ExpressionProposalProvider implements IContentProposalProvider {

    private final IdentifierParser identifierParser;

    private final IExpression expression;

    public ExpressionProposalProvider(IExpression expression) {
        this(expression, new IdentifierParser(expression, expression.getIpsProject()));
    }

    public ExpressionProposalProvider(IExpression expression, IdentifierParser identifierParser) {
        ArgumentCheck.notNull(expression);
        this.expression = expression;
        this.identifierParser = identifierParser;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String consideredInput = getConsideredInput(contents, position);

        List<IContentProposal> result = new LinkedList<IContentProposal>();
        addIdentifierNodes(consideredInput, result);
        addMatchingFunctions(consideredInput, result);
        return result.toArray(new IContentProposal[result.size()]);
    }

    private String getConsideredInput(String contents, int cursorPosition) {
        String leftOfCursor = getContentLeftOfCursor(contents, cursorPosition);
        Token token = getLastToken(leftOfCursor);
        return getTokenTextOrEverything(leftOfCursor, token);
    }

    private String getContentLeftOfCursor(String contents, int cursorPosition) {
        return contents.substring(0, cursorPosition);
    }

    private Token getLastToken(String leftOfCursor) {
        JavaCharStream stream = new JavaCharStream(new StringReader(leftOfCursor));
        FlParserTokenManager tokenManager = new FlParserTokenManager(stream);
        return getLastValidToken(leftOfCursor, tokenManager);
    }

    private Token getLastValidToken(String leftOfCursor, FlParserTokenManager tokenManager) {
        Token currentToken = null;
        Token previousToken = null;
        do {
            previousToken = currentToken;
            currentToken = tokenManager.getNextToken();
        } while (isValidToken(leftOfCursor, currentToken));
        return previousToken;
    }

    private boolean isValidToken(String leftOfCursor, Token token) {
        return token.beginColumn < leftOfCursor.length() - 1;
    }

    private String getTokenTextOrEverything(String leftOfCursor, Token token) {
        if (token != null) {
            return token.image;
        } else {
            return leftOfCursor;
        }
    }

    private void addIdentifierNodes(String contents, List<IContentProposal> result) {
        List<IdentifierProposal> proposals = identifierParser.getProposals(contents);
        for (IdentifierProposal identifierProposal : proposals) {
            result.add(new ContentProposal(identifierProposal.getText(), identifierProposal.getText(),
                    identifierProposal.getDescription()));
        }
    }

    private void addMatchingFunctions(String prefix, List<IContentProposal> result) {
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
