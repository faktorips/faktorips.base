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
import org.faktorips.devtools.core.builder.Messages;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IndexBasedAssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifiedAssociationNode;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

public class AssociationParser extends TypeBasedIdentifierParser {

    private static final char QUALIFIER_START = '[';

    private static final char QUALIFIER_END = ']';

    private static final String QUALIFIER_QUOTATION = "\""; //$NON-NLS-1$

    public AssociationParser(IExpression expression, IIpsProject ipsProject) {
        super(expression, ipsProject);
    }

    @Override
    protected IdentifierNode parseInternal() {
        try {
            IAssociation association = getContextType().findAssociation(getAssociationName(), getIpsProject());
            if (association != null) {
                if (isQualifier()) {
                    return createQualifiedAssiciationNode(association);
                } else if (isIndex()) {
                    return createIndexBasedAssiciationNode(association);
                } else {
                    return createAssociationNode(association);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return null;
    }

    private QualifiedAssociationNode createQualifiedAssiciationNode(IAssociation association) throws CoreException {
        boolean listOfType = isListOfTypeDatatype() || association.is1ToManyIgnoringQualifier();
        return new QualifiedAssociationNode(association, getQualifier(), listOfType, getIpsProject());
    }

    private IdentifierNode createIndexBasedAssiciationNode(IAssociation association) throws CoreException {
        if (association.is1To1()) {
            return new InvalidIdentifierNode(Message.newError(
                    ExprCompiler.NO_INDEX_FOR_1TO1_ASSOCIATION,
                    NLS.bind(Messages.AbstractParameterIdentifierResolver_noIndexFor1to1Association0,
                            association.getName(), getQualifierOrIndex())));
        }
        try {
            return new IndexBasedAssociationNode(association, getIndex(), getIpsProject());
        } catch (NumberFormatException e) {
            return new InvalidIdentifierNode(Message.newError(ExprCompiler.UNKNOWN_QUALIFIER,
                    NLS.bind("The statement {0} is no valid qualifier nor a valid index.", getQualifierOrIndex())));
        }
    }

    private AssociationNode createAssociationNode(IAssociation association) throws CoreException {
        boolean listOfType = isListOfTypeDatatype() || association.is1ToMany();
        return new AssociationNode(association, listOfType, getIpsProject());
    }

    protected String getAssociationName() {
        if (isQualifierOrIndex()) {
            return getIdentifierPart().substring(0, getIndexOrQualifierStart());
        }
        return getIdentifierPart();
    }

    private boolean isQualifierOrIndex() {
        return getIndexOrQualifierStart() > 0;
    }

    private boolean isQualifier() {
        int qualifierStart = getIdentifierPart().indexOf(QUALIFIER_START + QUALIFIER_QUOTATION);
        int qualifierEnd = getIdentifierPart().indexOf(QUALIFIER_QUOTATION + QUALIFIER_END);
        return qualifierStart > 0 && qualifierStart < qualifierEnd;
    }

    private boolean isIndex() {
        return isQualifierOrIndex() && !isQualifier();
    }

    private int getIndexOrQualifierStart() {
        return getIdentifierPart().indexOf(QUALIFIER_START);
    }

    private int getIndexOrQualifierEnd() {
        return getIdentifierPart().indexOf(QUALIFIER_END);
    }

    private String getQualifier() {
        return getQualifierOrIndex().substring(1, getQualifierOrIndex().length() - 1);
    }

    private int getIndex() {
        return Integer.valueOf(getQualifierOrIndex());
    }

    private String getQualifierOrIndex() {
        return getIdentifierPart().substring(getIndexOrQualifierStart() + 1, getIndexOrQualifierEnd());
    }

}
