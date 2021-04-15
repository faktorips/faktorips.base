/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierProposal;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.parser.FlParserConstants;
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

    private LinkedList<IContentProposal> proposals;

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

        proposals = new LinkedList<>();
        addMatchingFunctions(consideredInput);
        addIdentifierNodes(consideredInput);
        return proposals.toArray(new IContentProposal[proposals.size()]);
    }

    private String getConsideredInput(String contents, int cursorPosition) {
        String leftOfCursor = getContentLeftOfCursor(contents, cursorPosition);
        return getLastIdentifier(leftOfCursor);
    }

    private String getContentLeftOfCursor(String contents, int cursorPosition) {
        return contents.substring(0, cursorPosition);
    }

    /**
     * Sadly a hacky method. The basic problem is that in the context of ExpressionProposals an
     * incomplete string must be parsed. The generated FlParser and TokenManager have a problem with
     * incomplete tokens. The IdentifierParsers on the other hand could work with incomplete
     * identifiers, but not with other tokens (e.g. function names as "IF()").
     * 
     * This method lets the TokenManager parse as much as it can of the incomplete input string. In
     * case of an error the remaining content is used and given to the identifier parsers in the
     * hope that they can process it and provide proposals.
     */
    private String getLastIdentifier(String leftOfCursor) {
        JavaCharStream stream = new JavaCharStream(new StringReader(leftOfCursor));
        FlParserTokenManager tokenManager = new FlParserTokenManager(stream);
        Token token = null;
        while (continueParsingAfter(token)) {
            // CSOFF: IllegalCatch
            // The token manager may throw any throwable like a LexialError or exceptions :(
            try {
                token = tokenManager.getNextToken();
                if (isLastToken(token, leftOfCursor)) {
                    return getIdentifierText(token);
                }
            } catch (Throwable t) {
                /*
                 * SW 2.5.2014: Even though this a very common case for context proposals, the
                 * TokenManager cannot parse "p.coverages[" as a single identifier. It expects [0]
                 * or ["..."] in an identifier, but cannot find a corresponding closing bracket. The
                 * longest identifier it can match is "p.coverages". Continuing with "[" causes an
                 * error as the bracket on its own is never allowed by the defined grammar.
                 * 
                 * Our only choice is to catch the exception and use the whole remaining content
                 * (including the bracket) to calculate proposals.
                 */
                return getRemainingContent(token, leftOfCursor);
            }
            // CSON: IllegalCatch
        }
        return leftOfCursor;
    }

    private boolean continueParsingAfter(Token token) {
        return token == null || token.kind != FlParserConstants.EOF;
    }

    private String getIdentifierText(Token token) {
        if (token.kind == FlParserConstants.IDENTIFIER) {
            return token.image;
        } else {
            return StringUtils.EMPTY;
        }
    }

    private String getRemainingContent(Token lastValidToken, String leftOfCursor) {
        if (lastValidToken != null) {
            return leftOfCursor.substring(lastValidToken.getStartPositionRelativeTo(leftOfCursor));
        } else {
            return leftOfCursor;
        }
    }

    private boolean isLastToken(Token token, String leftOfCursor) {
        return token != null && token.getEndPositionRelativeTo(leftOfCursor) >= leftOfCursor.length() - 1;
    }

    private void addIdentifierNodes(String contents) {
        List<IdentifierProposal> identifierProposals = identifierParser.getProposals(contents);
        Collections.sort(identifierProposals);
        List<IContentProposal> preFunctionIdentifiers = new ArrayList<>();
        for (IdentifierProposal identifierProposal : identifierProposals) {
            if (identifierProposal.getNodeType().getProposalSortOrder() < 0) {
                preFunctionIdentifiers.add(createContentProposal(identifierProposal));
            } else {
                proposals.add(createContentProposal(identifierProposal));
            }
        }
        proposals.addAll(0, preFunctionIdentifiers);
    }

    private ContentProposal createContentProposal(IdentifierProposal identifierProposal) {
        return new ContentProposal(identifierProposal.getText(), identifierProposal.getLabel(),
                identifierProposal.getDescription(), identifierProposal.getPrefix());
    }

    private void addMatchingFunctions(String prefix) {
        ExprCompiler<JavaCodeFragment> compiler = expression.newExprCompiler(expression.getIpsProject());
        FlFunction<JavaCodeFragment>[] functions = compiler.getFunctions();
        for (FlFunction<JavaCodeFragment> function : functions) {
            if (checkMatchingNameWithCaseInsensitive(function.getName(), prefix)) {
                addFunctionToResult(function, prefix);
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

    private void addFunctionToResult(FlFunction<JavaCodeFragment> function, String prefix) {
        String name = function.getName();
        StringBuilder displayText = new StringBuilder(name);
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
        proposals.add(proposal);
    }
}
