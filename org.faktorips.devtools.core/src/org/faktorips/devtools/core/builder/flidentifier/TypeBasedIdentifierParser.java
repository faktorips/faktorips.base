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
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.type.IType;

/**
 * This parser subclass is specialized for parsing any parts of a type like associations or
 * attributes. The context type will always be a subclass of {@link IType} or a list of
 * {@link IType} using {@link ListOfTypeDatatype}.
 * 
 * @author dirmeier
 */
public abstract class TypeBasedIdentifierParser extends AbstractIdentifierNodeParser {

    public TypeBasedIdentifierParser(IExpression expression, IIpsProject ipsProject) {
        super(expression, ipsProject);
    }

    @Override
    protected final IdentifierNode parse() {
        if (isAllowedType()) {
            return parseInternal();
        } else {
            return null;
        }
    }

    protected boolean isAllowedType() {
        if (isAllowedType(super.getContextType())) {
            return true;
        }
        if (isListOfTypeDatatype()) {
            ListOfTypeDatatype listOfTypeDatatype = (ListOfTypeDatatype)super.getContextType();
            return isAllowedType(listOfTypeDatatype.getBasicDatatype());
        }
        return false;
    }

    protected boolean isAllowedType(Datatype type) {
        return type instanceof IType;
    }

    @Override
    public IType getContextType() {
        if (isListOfTypeDatatype()) {
            ListOfTypeDatatype listOfTypeDatatype = (ListOfTypeDatatype)super.getContextType();
            return (IType)listOfTypeDatatype.getBasicDatatype();
        } else {
            return (IType)super.getContextType();
        }
    }

    protected boolean isListOfTypeDatatype() {
        return super.getContextType() instanceof ListOfTypeDatatype;
    }

    /**
     * Implement this method in subclasses instead of {@link #parse()} to have the type safety
     * check.
     * 
     * @see #parse()
     * 
     * @return The parsed {@link IdentifierNode} or null if this parser is not responsible for the
     *         identifier part
     */
    protected abstract IdentifierNode parseInternal();
}
