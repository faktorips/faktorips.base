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

import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.util.TextRegion;

/**
 * This is the abstract class for all identifier parsers. An identifier parser tries to parse an
 * identifier string and creates an {@link IdentifierNode}.
 * 
 * @author dirmeier
 */
public abstract class AbstractIdentifierNodeParser {

    private final IExpression expression;

    private final IIpsProject ipsProject;

    private String identifierPart;

    private Datatype contextType;

    private IdentifierNode previousNode;

    private TextRegion textRegion;

    /**
     * Creates the identifier parser and store the {@link IExpression} as well as the used
     * {@link IIpsProject}.
     * 
     * @param expression The expression where the identifier was entered
     * @param ipsProject The {@link IIpsProject} that we use to search other IPS objects.
     */
    public AbstractIdentifierNodeParser(IExpression expression, IIpsProject ipsProject) {
        this.expression = expression;
        this.ipsProject = ipsProject;
        setContextType(expression.findProductCmptType(ipsProject));
    }

    /**
     * This method is called to parse the identifier part string with the given context type.
     * 
     * @param identifierPart The part of the identifier that should be parsed
     * @param previousNode The previous node that was already parsed. May be null if there is no
     *            previous identifier part
     * 
     * @return The parsed identifier node or null if this parser is not responsible
     */
    public IdentifierNode parse(String identifierPart, IdentifierNode previousNode, TextRegion textRegion) {
        this.textRegion = textRegion;
        this.setIdentifierPart(identifierPart);
        if (previousNode == null) {
            this.setContextType(expression.findProductCmptType(getIpsProject()));
        } else {
            this.setContextType(previousNode.getDatatype());
        }
        this.setPreviousNode(previousNode);
        IdentifierNode identifierNode = parse();
        return identifierNode;
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
     *         this parser cannot parse the identifier.
     */
    protected abstract IdentifierNode parse();

    public IExpression getExpression() {
        return expression;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public void setIdentifierPart(String identifierPart) {
        this.identifierPart = identifierPart;
    }

    protected String getIdentifierPart() {
        return identifierPart;
    }

    public Datatype getContextType() {
        return contextType;
    }

    public void setContextType(Datatype contextType) {
        this.contextType = contextType;
    }

    protected boolean isContextTypeFormulaType() {
        return getContextType() == getExpression().findProductCmptType(getIpsProject());
    }

    /**
     * Returns the previous node that was parsed using the previous identifier part. The previous
     * node may be null if this is the first part of the identifier
     * 
     * @return The previous parsed identifier node or null if there is none.
     */
    public IdentifierNode getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(IdentifierNode previousNode) {
        this.previousNode = previousNode;
    }

    /**
     * Use this {@link IdentifierNodeFactory} to create any kind of {@link IdentifierNode nodes}.
     * 
     * @return The {@link IdentifierNodeFactory} to create a new {@link IdentifierNode}
     */
    public IdentifierNodeFactory nodeFactory() {
        return new IdentifierNodeFactory(getIdentifierPart(), getTextRegion(), getIpsProject());
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
     * Returns every possible identifier node that could be parsed by this parser given the current
     * context type and previous node. Before you call this method you have to setup the parser
     * context by calling {@link #parse(String, IdentifierNode, TextRegion)}. The result is filtered
     * by the given prefix, that means only nodes with a text that starts with the given prefix
     * should be included in the result list. The prefix compare could be case insensitive.
     * 
     * @param prefix The prefix text to filter the result. To get all available proposals the prefix
     *            needs to be an empty string.
     * 
     * @return A list of {@link IdentifierNode nodes} that could be parsed by this node parser given
     *         the current parser state.
     */
    public abstract List<IdentifierNode> getProposals(String prefix);
}
