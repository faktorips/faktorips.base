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

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.type.IType;

/**
 * This parser subclass is specialized for parsing any parts of a type like associations or
 * attributes. The context type will always be a subclass of {@link IType} or a list of
 * {@link IType} using {@link ListOfTypeDatatype}.
 * 
 * @author dirmeier
 */
public abstract class TypeBasedIdentifierParser extends AbstractIdentifierNodeParser {

    public TypeBasedIdentifierParser(ParsingContext parsingContext) {
        super(parsingContext);
    }

    @Override
    protected final IdentifierNode parse() {
        if (isAllowedType()) {
            return parseInternal();
        } else {
            return null;
        }
    }

    @Override
    public boolean isAllowedType() {
        if (isAllowedType(super.getContextType())) {
            return true;
        }
        if (isListOfTypeContext()) {
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
        if (isListOfTypeContext()) {
            ListOfTypeDatatype listOfTypeDatatype = (ListOfTypeDatatype)super.getContextType();
            return (IType)listOfTypeDatatype.getBasicDatatype();
        } else {
            return (IType)super.getContextType();
        }
    }

    protected boolean isListOfTypeContext() {
        return super.getContextType() instanceof ListOfTypeDatatype;
    }

    /**
     * Implement this method in subclasses instead of {@link #parse()} to have the type safety
     * check.
     * 
     * @see #parse()
     * 
     * @return The parsed {@link IdentifierNode} or null if this parser is not responsible for the
     *             identifier part
     */
    protected abstract IdentifierNode parseInternal();
}
