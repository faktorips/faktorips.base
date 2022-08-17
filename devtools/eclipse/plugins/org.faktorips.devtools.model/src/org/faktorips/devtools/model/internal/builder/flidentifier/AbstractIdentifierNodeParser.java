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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IMultiLanguageSupport;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.util.TextRegion;

/**
 * This is the abstract class for all identifier parsers. An identifier parser tries to parse an
 * identifier string and creates an {@link IdentifierNode}.
 * 
 * @author dirmeier
 */
public abstract class AbstractIdentifierNodeParser {

    public static final String NAME_DESCRIPTION_SEPERATOR = " - "; //$NON-NLS-1$

    private final ParsingContext parsingContext;

    private Datatype contextType;

    private TextRegion textRegion;

    /**
     * Creates the identifier parser and store the {@link IExpression} as well as the used
     * {@link IIpsProject}.
     * 
     * @param parsingContext The context that holds the status of parsing of the whole identifier
     */
    public AbstractIdentifierNodeParser(ParsingContext parsingContext) {
        this.parsingContext = parsingContext;
        setContextType(getExpression().findProductCmptType(getIpsProject()));
    }

    public ParsingContext getParsingContext() {
        return parsingContext;
    }

    /**
     * This method is called to parse the identifier part string with the given context type.
     * 
     * @param textRegion The text and region of the identifier specifying the current identifier
     *            part
     * 
     * @return The parsed identifier node or null if this parser is not responsible
     */
    public IdentifierNode parse(TextRegion textRegion) {
        this.textRegion = textRegion;
        if (getPreviousNode() == null) {
            setContextType(getExpression().findProductCmptType(getIpsProject()));
        } else {
            setContextType(getPreviousNode().getDatatype());
        }
        return parse();
    }

    /**
     * This method is implemented by the different parsers to parse the current identifier part,
     * retrieved from {@link #getIdentifierPart()}, To know the current datatype of the previous
     * parsing step you could call {@link #getContextType()}.
     * <p>
     * If the parser cannot parse the identifier part, this method should return null. The parsing
     * will be continued with the next parser.
     * <p>
     * In case of any exception during parsing, this method should return an
     * {@link InvalidIdentifierNode} if and only if the identifier part could be parsed in normal
     * circumstances. An {@link InvalidIdentifierNode} means that the identifier part could be
     * parsed but there is any error. The parsing will stop.
     * 
     * @return The {@link IdentifierNode} that matches to the current identifier part or null if
     *             this parser cannot parse the identifier.
     */
    protected abstract IdentifierNode parse();

    public IExpression getExpression() {
        return getParsingContext().getExpression();
    }

    public IIpsProject getIpsProject() {
        return getParsingContext().getIpsProject();
    }

    protected String getIdentifierPart() {
        return textRegion.getTextRegionString();
    }

    /**
     * Returns the current context type. This class may throw a {@link ClassCastException} if the
     * current type is not allowed. Check {@link #isAllowedType()} before calling this method.
     * <p>
     * If you override this method and want to cast to a specific Datatype you can do this safely if
     * you also override {@link #isAllowedType()} accordingly.
     * 
     * @return The datatype of the context returned by the previous node or the type of the
     *             expression signature.
     */
    public Datatype getContextType() {
        return contextType;
    }

    /**
     * Checks whether the current context type is allowed or not. Call this method to check whether
     * it is safe to call {@link #getContextType()}.
     */
    public boolean isAllowedType() {
        return true;
    }

    public void setContextType(Datatype contextType) {
        this.contextType = contextType;
    }

    protected boolean isContextTypeFormulaType() {
        return isAllowedType() && getContextType() == getExpression().findProductCmptType(getIpsProject());
    }

    /**
     * Returns the previous node that was parsed using the previous identifier part. The previous
     * node may be null if this is the first part of the identifier
     * 
     * @return The previous parsed identifier node or null if there is none.
     */
    public IdentifierNode getPreviousNode() {
        return getParsingContext().getPreviousNode();
    }

    /**
     * Returns an iterator with the previous nodes. The first element is the latest node which is
     * also returned by {@link #getPreviousNode()}. The iterator traverses from the latest node to
     * the first one.
     * 
     */
    public LinkedList<IdentifierNode> getPreviousNodes() {
        return getParsingContext().getNodes();
    }

    /**
     * Use this {@link IdentifierNodeFactory} to create any kind of {@link IdentifierNode nodes}.
     * 
     * @return The {@link IdentifierNodeFactory} to create a new {@link IdentifierNode}
     */
    public IdentifierNodeFactory nodeFactory() {
        return new IdentifierNodeFactory(getTextRegion(), getIpsProject());
    }

    /**
     * The text region that matches the current parser position. It defines the region that is
     * parsed by this node parser of the whole identifier that is parsed by the
     * {@link IdentifierParser}.
     * 
     * @return The text region that corresponds to the text part that is parsed by this node parser
     */
    public TextRegion getTextRegion() {
        return textRegion;
    }

    /**
     * Returns every possible identifier node that this parser can provide based on this parsers'
     * state (the current context type and the predecessor node) and the user input (prefix).
     * <p>
     * The context type and the predecessor node are used to calculate all possible identifier nodes
     * of the node type this parser is responsible for, the prefix is then used to filter those
     * nodes. Only nodes whose texts start with the given prefix (case insensitive) are returned.
     * <p>
     * Before calling this method the parser context must be set up, e.g. by calling
     * {@link #parse(TextRegion)}.
     * 
     * @param prefix The prefix text to filter the result. Empty string to get all available
     *            proposals.
     * 
     * @return A list of {@link IdentifierNode nodes} that are possible given the current parser
     *             state and the current input (prefix).
     */
    public abstract List<IdentifierProposal> getProposals(String prefix);

    protected String getNameAndDescription(IIpsElement ipsElement, IMultiLanguageSupport multiLanguageSupport) {
        return getNameAndDescription(getName(ipsElement, multiLanguageSupport),
                getDescription(ipsElement, multiLanguageSupport));
    }

    protected String getNameAndDescription(String name, String description) {
        StringBuilder result = new StringBuilder();
        result.append(name);
        if (StringUtils.isNotBlank(description)) {
            result.append(NAME_DESCRIPTION_SEPERATOR).append(description);
        }
        return result.toString();
    }

    protected String getName(IIpsElement ipsElement, IMultiLanguageSupport multiLanguageSupport) {
        if (ipsElement instanceof ILabeledElement) {
            return multiLanguageSupport.getLocalizedLabel((ILabeledElement)ipsElement);
        } else {
            return ipsElement.getName();
        }
    }

    protected String getDescription(IIpsElement ipsElement, IMultiLanguageSupport multiLanguageSupport) {
        if (ipsElement instanceof IDescribedElement) {
            return multiLanguageSupport.getLocalizedDescription((IDescribedElement)ipsElement);
        } else {
            return null;
        }
    }

}
