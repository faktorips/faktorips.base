/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Region;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

/**
 * The {@link IdentifierParser} is used to parse an identifier of an expression. It uses a list of
 * {@link AbstractIdentifierNodeParser node parsers} and iterates over these parser until it
 * receives an {@link IdentifierNode}.
 * 
 * @author dirmeier
 */
public class IdentifierParser {

    private static final String IDENTIFIER_SEPERATOR_REGEX = "[\\.\\[]"; //$NON-NLS-1$

    private static final Pattern IDENTIFIER_SEPERATOR_PATTERN = Pattern.compile(IDENTIFIER_SEPERATOR_REGEX);

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
     * Call this method to parse a identifier. An identifier may consists of several parts,
     * separated by '.'. Every part will be represented by another {@link IdentifierNode} that are
     * chained by their successor. This method will return the {@link IdentifierNode} that
     * represents the first part of the identifier. You can ask this {@link IdentifierNode} for the
     * following parts by calling {@link IdentifierNode#getSuccessor()}. If there is no following
     * part, the successor is <code>null</code>.
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
            IdentifierNode node = parser.parse(identifierPart, previousNode, matcher.getRegion());
            if (node != null) {
                if (matcher.hasNextIdentifierPart()) {
                    matcher.nextIdentifierPart();
                    node.setSuccessor(parseNextPart(node));
                }
                return node;
            }
        }
        return new IdentifierNodeFactory(identifierPart, matcher.getRegion(), ipsProject)
                .createInvalidIdentifier(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER,
                        NLS.bind(Messages.IdentifierParser_msgErrorInvalidIdentifier, identifierPart)));
    }

    private static class IdentifierMatcher {

        private final String identifier;

        private final Matcher matcher;

        private Region region;

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
                region = new Region(sequenceStart, matcher.start() - sequenceStart);
            } else {
                region = new Region(sequenceStart, identifier.length() - sequenceStart);
            }
        }

        public boolean hasNextIdentifierPart() {
            return !matcher.hitEnd();
        }

        public String getIdentifierPart() {
            return identifier.substring(region.getOffset(), region.getOffset() + region.getLength());
        }

        public Region getRegion() {
            return region;
        }

    }

}
