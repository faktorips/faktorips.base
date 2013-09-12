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

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.parser.ParseException;
import org.faktorips.util.message.Message;

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
        this.expression = expression;
        this.ipsProject = ipsProject;
        parsers = new ArrayList<AbstractIdentifierNodeParser>();
        parsers.add(new ParameterParser(expression, ipsProject));
        parsers.add(new AttributeParser(expression, ipsProject, identifierFilter));
        parsers.add(new AssociationParser(expression, ipsProject));
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
     * @throws ParseException If any part of the identifier could not be parsed, the method will
     *             return a {@link ParseException}.
     */
    public IdentifierNode parse(String identifier) throws ParseException {
        String[] identifierParts = identifier.split(IDENTIFIER_SEPERATOR_REGEX);
        contextType = expression.findProductCmptType(ipsProject);
        this.identifierParts = identifierParts;
        this.currentPartIndex = 0;
        return parseNextPart();
    }

    private IdentifierNode parseNextPart() {
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
        return new InvalidIdentifierNode(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER,
                NLS.bind("The statement {0} is no valid identifier.", getIdentifierPart())));
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
