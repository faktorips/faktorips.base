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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

/**
 * This parser tires to match the identifier node to an association of the context type. It also
 * handles optional qualifiers or indices. The output will be an {@link AssociationNode} or a
 * subclass of this class.
 * 
 * 
 * @author dirmeier
 */
public class AssociationParser extends TypeBasedIdentifierParser {

    public AssociationParser(IExpression expression, IIpsProject ipsProject) {
        super(expression, ipsProject);
    }

    @Override
    protected boolean isAllowedType() {
        return super.isAllowedType() && super.getContextType() instanceof IPolicyCmptType;
    }

    @Override
    public IPolicyCmptType getContextType() {
        return (IPolicyCmptType)super.getContextType();
    }

    @Override
    protected IdentifierNode parseInternal() {
        IPolicyCmptType policyCmptType = getContextType();
        try {
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)policyCmptType.findAssociation(
                    getAssociationName(), getIpsProject());
            if (association != null) {
                return parseAssociation(association);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return nodeFactory().createInvalidIdentifier(
                    Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, NLS.bind(
                            Messages.AssociationParser_msgErrorWhileFindAssociation, getIdentifierPart(),
                            getContextType())));
        }
        return null;
    }

    private IdentifierNode parseAssociation(IPolicyCmptTypeAssociation association) {
        return nodeFactory().createAssociationNode(association, isListOfTypeContext());
    }

    protected String getAssociationName() {
        return getIdentifierPart();
    }

}
