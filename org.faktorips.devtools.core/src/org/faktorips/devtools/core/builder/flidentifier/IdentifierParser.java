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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.internal.refactor.TextRegion;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

/**
 * Parses the identifiers of an expression. It uses a list of {@link AbstractIdentifierNodeParser
 * node parsers} for the different types of identifiers. Each identifier is parsed to an
 * {@link IdentifierNode}.
 * 
 * @author dirmeier
 */
public class IdentifierParser {

    private final IIpsProject ipsProject;

    private final ArrayList<AbstractIdentifierNodeParser> parsers;

    private IdentifierMatcher matcher;

    /**
     * Creates the {@link IdentifierParser} for the specified expression. The {@link IIpsProject} is
     * used for searching other IPS objects
     * 
     * @param expression The expression which is currently parsed
     * @param ipsProject The {@link IIpsProject} to search other {@link IIpsObject IPS objects}
     */
    public IdentifierParser(IExpression expression, IIpsProject ipsProject) {
        this(expression, ipsProject, IpsPlugin.getDefault().getIdentifierFilter());
    }

    /**
     * Creates the {@link IdentifierParser} for the specified expression. The {@link IIpsProject} is
     * used for searching other IPS objects
     * 
     * @param expression The expression which is currently parsed
     * @param ipsProject The {@link IIpsProject} to search other {@link IIpsObject IPS objects}
     */
    public IdentifierParser(IExpression expression, IIpsProject ipsProject, IdentifierFilter identifierFilter) {
        this.ipsProject = ipsProject;
        parsers = new ArrayList<AbstractIdentifierNodeParser>();
        parsers.add(new ParameterParser(expression, ipsProject));
        parsers.add(new AttributeParser(expression, ipsProject, identifierFilter));
        parsers.add(new AssociationParser(expression, ipsProject));
        parsers.add(new QualifierAndIndexParser(expression, ipsProject));
        parsers.add(new EnumParser(expression, ipsProject));
    }

    /**
     * Call this method to parse an identifier string. An identifier may consist of several parts
     * separated by '.','[' and ']'. Each part is parsed to an {@link IdentifierNode} that is linked
     * to its successor, similar to a linked list.
     * 
     * This method will return the {@link IdentifierNode} that represents the first part of the
     * identifier. The first {@link IdentifierNode} is also the head of the linked list of all
     * identifier nodes. Thus the nodes for all following parts can be accessed by calling
     * {@link IdentifierNode#getSuccessor()}. If there is no following part, the successor is
     * <code>null</code>.
     * 
     * @param identifier The identifier, for example param1.attributeOne
     * @return The {@link IdentifierNode} of the first part of the identifier
     * 
     */
    public IdentifierNode parse(String identifier) {
        matcher = new IdentifierMatcher(identifier);
        return parseNextPart(null);
    }

    private IdentifierNode parseNextPart(IdentifierNode previousNode) {
        String identifierPart = matcher.getIdentifierPart();
        for (AbstractIdentifierNodeParser parser : parsers) {
            IdentifierNode node = parser.parse(identifierPart, previousNode, matcher.getTextRegion());
            if (node != null) {
                if (matcher.hasNextIdentifierPart()) {
                    matcher.nextIdentifierPart();
                    node.setSuccessor(parseNextPart(node));
                }
                return node;
            }
        }
        return new IdentifierNodeFactory(identifierPart, matcher.getTextRegion(), ipsProject)
                .createInvalidIdentifier(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER,
                        NLS.bind(Messages.IdentifierParser_msgErrorInvalidIdentifier, identifierPart)));
    }

    protected static class IdentifierMatcher {

        private static final String IDENTIFIER_SEPERATOR_REGEX = "[\\.\\[]"; //$NON-NLS-1$

        private static final Pattern IDENTIFIER_SEPERATOR_PATTERN = Pattern.compile(IDENTIFIER_SEPERATOR_REGEX);

        private final String identifier;

        private final Matcher matcher;

        private TextRegion textRegion;

        public IdentifierMatcher(String identifier) {
            this.identifier = identifier;
            this.matcher = IDENTIFIER_SEPERATOR_PATTERN.matcher(identifier);
            find(0);
        }

        public void nextIdentifierPart() {
            int sequenceStart = matcher.end();
            find(sequenceStart);
        }

        private void find(int sequenceStart) {
            if (matcher.find()) {
                textRegion = new TextRegion(sequenceStart, matcher.start());
            } else {
                textRegion = new TextRegion(sequenceStart, identifier.length());
            }
        }

        public boolean hasNextIdentifierPart() {
            return !matcher.hitEnd();
        }

        public String getIdentifierPart() {
            return textRegion.getSubstring(identifier);
        }

        public TextRegion getTextRegion() {
            return textRegion;
        }

    }

}
