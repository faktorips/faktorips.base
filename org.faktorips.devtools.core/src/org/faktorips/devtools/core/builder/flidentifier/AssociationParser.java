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
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IndexBasedAssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifiedAssociationNode;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.type.IAssociation;

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

    private QualifiedAssociationNode createQualifiedAssiciationNode(IAssociation association) {
        try {
            boolean listOfType = isListOfTypeDatatype() || association.is1ToManyIgnoringQualifier();
            return new QualifiedAssociationNode(association, getQualifier(), listOfType, getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IndexBasedAssociationNode createIndexBasedAssiciationNode(IAssociation association) {
        try {
            return new IndexBasedAssociationNode(association, getIndex(), getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
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
        return getIdentifierPart().indexOf(QUALIFIER_START + QUALIFIER_QUOTATION) > 0;
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
        if (getQualifierOrIndex().startsWith(QUALIFIER_QUOTATION)
                && getQualifierOrIndex().endsWith(QUALIFIER_QUOTATION)) {
            return getQualifierOrIndex().substring(1, getQualifierOrIndex().length() - 1);
        } else {
            throw new IllegalArgumentException("Illegal qualifier syntax " + getQualifierOrIndex()); //$NON-NLS-1$
        }
    }

    private int getIndex() {
        return Integer.valueOf(getQualifierOrIndex());
    }

    private String getQualifierOrIndex() {
        return getIdentifierPart().substring(getIndexOrQualifierStart() + 1, getIndexOrQualifierEnd());
    }

}
