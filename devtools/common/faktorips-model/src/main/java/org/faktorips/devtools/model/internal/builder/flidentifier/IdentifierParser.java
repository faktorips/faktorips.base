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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.fl.IdentifierFilter;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.Message;

/**
 * Parses the identifiers of an expression. It uses a list of {@link AbstractIdentifierNodeParser
 * node parsers} for the different types of identifiers. Each identifier is parsed to an
 * {@link IdentifierNode}.
 * 
 * @author dirmeier
 */
public class IdentifierParser {

    private static final char DEFAULT_SEPERATOR = '.';

    private static final char QUALIFIER_SEPERATOR = '[';

    private final IdentifierFilter identifierFilter;

    private final ParsingContext parsingContext;

    /**
     * Maps the identifier node parsers to their specific separator character. For example the
     * {@link QualifierAndIndexParser} maps to the separator character "[", as all qualifiers and
     * indices start with that separator.
     */
    private final Map<AbstractIdentifierNodeParser, Character> parsers = new LinkedHashMap<>();

    private IdentifierMatcher matcher;

    /**
     * Creates the {@link IdentifierParser} for the specified expression. The {@link IIpsProject} is
     * used for searching other IPS objects
     * 
     * @param expression The expression which is currently parsed
     * @param ipsProject The {@link IIpsProject} to search other {@link IIpsObject IPS objects}
     */
    public IdentifierParser(IExpression expression, IIpsProject ipsProject) {
        this(expression, ipsProject, IIpsModelExtensions.get().getIdentifierFilter());
    }

    /**
     * Creates the {@link IdentifierParser} for the specified expression. The {@link IIpsProject} is
     * used for searching other IPS objects
     * 
     * @param expression The expression which is currently parsed
     * @param ipsProject The {@link IIpsProject} to search other {@link IIpsObject IPS objects}
     */
    public IdentifierParser(IExpression expression, IIpsProject ipsProject, IdentifierFilter identifierFilter) {
        parsingContext = new ParsingContext(expression, ipsProject);
        this.identifierFilter = identifierFilter;
        initParsers();
    }

    private void initParsers() {
        parsers.put(new ParameterParser(parsingContext), DEFAULT_SEPERATOR);
        parsers.put(new AttributeParser(parsingContext, identifierFilter), DEFAULT_SEPERATOR);
        parsers.put(new AssociationParser(parsingContext), DEFAULT_SEPERATOR);
        parsers.put(new QualifierAndIndexParser(parsingContext), QUALIFIER_SEPERATOR);
        parsers.put(new EnumParser(parsingContext), DEFAULT_SEPERATOR);
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
        initNewParse(identifier);
        return parseNextPart();
    }

    private IdentifierNode parseNextPart() {
        for (Entry<AbstractIdentifierNodeParser, Character> parserEntry : parsers.entrySet()) {
            if (isResponsibleForCurrentTextRegion(parserEntry)) {
                IdentifierNode node = parserEntry.getKey().parse(matcher.getTextRegion());
                if (node != null) {
                    if (matcher.hasNextIdentifierPart()) {
                        parsingContext.pushNode(node);
                        matcher.nextIdentifierPart();
                        node.setSuccessor(parseNextPart());
                    }
                    return node;
                }
            }
        }
        return new IdentifierNodeFactory(matcher.getTextRegion(), parsingContext.getIpsProject())
                .createInvalidIdentifier(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER,
                        MessageFormat.format(Messages.IdentifierParser_msgErrorInvalidIdentifier,
                                matcher.getIdentifierPart())));
    }

    /**
     * Returns <code>true</code> if the parser contained in the entry is responsible for the current
     * text region, <code>false</code> else. This is done by checking the character preceding the
     * current text region against the parsers specific separator character.
     * 
     * @param parserEntry the entry containing the parser and the corresponding separator character
     */
    private boolean isResponsibleForCurrentTextRegion(Entry<AbstractIdentifierNodeParser, Character> parserEntry) {
        return isParserSpecificSeparator(parserEntry.getValue());
    }

    /**
     * Returns <code>true</code> if the given separator character precedes the current text region
     * (the part of text that is currently being looked at), <code>false</code> else.
     * 
     * @param separator the separator character
     */
    private boolean isParserSpecificSeparator(char separator) {
        TextRegion textRegion = matcher.getTextRegion();
        return textRegion.getStart() == 0 || textRegion.isRelativeChar(-1, separator);
    }

    /**
     * Returns every possible identifier proposal that the parsers can provide. However, only
     * provides proposals if the syntax rules are adhered to. For example "coverage[0." will not
     * provide proposals, as "." is illegal inside "[]".
     * 
     * @param existingContent the text to filter the result. Empty string to get all available
     *            proposals.
     * 
     * @return a list {@link IdentifierProposal IdentifierProposals}.
     */
    public List<IdentifierProposal> getProposals(String existingContent) {
        parse(existingContent);
        ArrayList<IdentifierProposal> result = new ArrayList<>();
        if (isLegalSyntaxUpToNow()) {
            for (Entry<AbstractIdentifierNodeParser, Character> parserEntry : parsers.entrySet()) {
                if (isResponsibleForCurrentTextRegion(parserEntry)) {
                    result.addAll(getProposalsFrom(parserEntry));
                }
            }
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the identifier's syntax is correct up to (and including) the
     * currently processed text region, <code>false</code> if it contains a syntax error.
     * 
     * {@link IdentifierMatcher#hitEnd()} is used as an indicator for such an error. The method
     * returns <code>false</code> if no valid region could be matched, and thus no valid closing
     * character for an identifier part could be found. This is the case if an illegal character was
     * entered in between.
     * 
     * For example "coverage[0.]" will not hitEnd(), as the matcher tries to associate two following
     * separators "[" and ".", but they do not belong together. On the other hand "coverage[0]" will
     * hitEnd(), as the matcher identifies the braces "[" and "]" as belonging together.
     */
    private boolean isLegalSyntaxUpToNow() {
        return matcher.hitEnd();
    }

    /**
     * Returns the proposals the parser in the entry provides given the current text region (or
     * identifier part).
     * 
     * @param parserEntry the entry containing the parser
     */
    private List<IdentifierProposal> getProposalsFrom(Entry<AbstractIdentifierNodeParser, Character> parserEntry) {
        return parserEntry.getKey().getProposals(matcher.getIdentifierPart());
    }

    private void initNewParse(String identifier) {
        parsingContext.init();
        matcher = new IdentifierMatcher(identifier);
    }

    protected static class IdentifierMatcher {

        private static final String IDENTIFIER_SEPERATOR_REGEX = "[\\" + DEFAULT_SEPERATOR + "\\" + QUALIFIER_SEPERATOR //$NON-NLS-1$ //$NON-NLS-2$
                + "]"; //$NON-NLS-1$

        private static final Pattern IDENTIFIER_SEPERATOR_PATTERN = Pattern.compile(IDENTIFIER_SEPERATOR_REGEX);

        private final String identifier;

        private final Matcher matcher;

        private TextRegion textRegion;

        public IdentifierMatcher(String identifier) {
            this.identifier = identifier;
            matcher = IDENTIFIER_SEPERATOR_PATTERN.matcher(identifier);
            find(0);
        }

        public void nextIdentifierPart() {
            int sequenceStart = matcher.end();
            find(sequenceStart);
        }

        private void find(int sequenceStart) {
            if (matcher.find()) {
                textRegion = new TextRegion(identifier, sequenceStart, matcher.start());
            } else {
                textRegion = new TextRegion(identifier, sequenceStart, identifier.length());
            }
        }

        public boolean hasNextIdentifierPart() {
            return !matcher.hitEnd();
        }

        public boolean hitEnd() {
            return matcher.hitEnd();
        }

        /**
         * @return the value of the current text region.
         */
        public String getIdentifierPart() {
            return textRegion.getTextRegionString();
        }

        /**
         * @return the region of text that is currently being processed.
         */
        public TextRegion getTextRegion() {
            return textRegion;
        }

    }

}
