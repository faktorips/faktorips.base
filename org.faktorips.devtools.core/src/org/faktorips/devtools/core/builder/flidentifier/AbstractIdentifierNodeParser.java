/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;

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
    }

    /**
     * This method is called to parse the identifier part string with the given context type.
     * 
     * @param identifierPart The part of the identifier that should be parsed
     * @param contextType The current context type, retrieved from previous parsing or the product
     *            component type of the formula
     * 
     * @return The parsed identifier node or null if this parser is not responsible
     */
    public IdentifierNode parse(String identifierPart, Datatype contextType) {
        this.setIdentifierPart(identifierPart);
        this.setContextType(contextType);
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
     * Use this {@link IdentifierNodeFactory} to create any kind of {@link IdentifierNode nodes}.
     * 
     * @return The {@link IdentifierNodeFactory} to create a new {@link IdentifierNode}
     */
    public IdentifierNodeFactory nodeFactory() {
        return new IdentifierNodeFactory(getIdentifierPart(), getIpsProject());
    }

}
