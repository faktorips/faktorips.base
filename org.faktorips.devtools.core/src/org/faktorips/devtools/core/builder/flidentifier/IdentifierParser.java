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

import java.util.ArrayList;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;

/**
 * The {@link IdentifierParser} is used to parse an identifier of an expression. It uses a list of
 * {@link AbstractIdentifierNodeParser node parsers} and iterates over these parser until it
 * receives an {@link IdentifierNode}.
 * 
 * @author dirmeier
 */
public class IdentifierParser {

    private static final String IDENTIFIER_SEPERATOR_REGEX = "\\."; //$NON-NLS-1$

    private final IExpression expression;
    private final IIpsProject ipsProject;
    private final ArrayList<AbstractIdentifierNodeParser> parsers;

    private Datatype contextType;

    private String[] identifierParts;

    private int currentPartIndex;

    /**
     * Creates the {@link IdentifierParser} for the specified expression. The {@link IIpsProject} is
     * used for searching other IPS objects
     * 
     * @param expression The expression which is currently parsed
     * @param ipsProject The {@link IIpsProject} to search other {@link IIpsObject IPS objects}
     */
    public IdentifierParser(IExpression expression, IIpsProject ipsProject) {
        this.expression = expression;
        this.ipsProject = ipsProject;
        parsers = new ArrayList<AbstractIdentifierNodeParser>();
        parsers.add(new ParameterParser(expression, ipsProject));
        parsers.add(new AttributeParser(expression, ipsProject));
        parsers.add(new AssociationParser(expression, ipsProject));
        parsers.add(new EnumParser(expression, ipsProject));
    }

    public IdentifierNode parse(String identifier) {
        String[] identifierParts = identifier.split(IDENTIFIER_SEPERATOR_REGEX);
        contextType = expression.findProductCmptType(ipsProject);
        this.identifierParts = identifierParts;
        this.currentPartIndex = 0;
        return parseNextPart();
    }

    protected IdentifierNode parseNextPart() {
        for (AbstractIdentifierNodeParser parser : parsers) {
            IdentifierNode node = parser.parse(getIdentifierPart(), contextType);
            if (node != null) {
                if (hasNextIdentifierPart()) {
                    nextIdentifierPart(node);
                    node.setSuccessor(parseNextPart());
                }
                return node;
            }
        }
        return null;
    }

    private String getIdentifierPart() {
        return identifierParts[currentPartIndex];
    }

    protected boolean hasNextIdentifierPart() {
        return identifierParts.length > currentPartIndex + 1;
    }

    protected void nextIdentifierPart(IdentifierNode parsedNode) {
        contextType = parsedNode.getDatatype();
        currentPartIndex++;
    }

}
