/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.util.ArgumentCheck;

/**
 * Datatype representing a list of objects of a basic {@link IType}.
 * <p>
 * This datatype is currently only used when navigating associations in expressions. Therefore no
 * {@link DatatypeHelper} exists and this datatype is not usable in attributes.
 * </p>
 * 
 * @see IExpression
 * @see IType
 * @since 3.6
 * @author schwering
 */
public class ListOfTypeDatatype extends AbstractDatatype implements ValueDatatype {

    private IType basicType;

    /**
     * Constructs a new List type based on the given underlying basic type.
     */
    public ListOfTypeDatatype(IType basicType) {
        super();
        ArgumentCheck.notNull(basicType);
        this.basicType = basicType;
    }

    /**
     * Returns the List type's basic type. E.g. for a list of policy values, <code>Policy</code> is
     * the basic type.
     */
    public Datatype getBasicDatatype() {
        return basicType;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public String getName() {
        StringBuffer buffer = new StringBuffer("List<"); //$NON-NLS-1$
        buffer.append('<');
        buffer.append(basicType.getName());
        buffer.append('>');
        return buffer.toString();
    }

    @Override
    public String getQualifiedName() {
        return getJavaClassName();
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    @Override
    public String getJavaClassName() {
        StringBuffer buffer = new StringBuffer(List.class.getName());
        buffer.append('<');
        buffer.append(basicType.getName());
        buffer.append('>');
        return buffer.toString();
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * <strong>Not supported yet.</strong> Always returns {@code false}.
     */
    @Override
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        // parsing of array value datatypes is not supported yet.
        return false;
    }

    /**
     * <strong>Not supported yet.</strong>
     * 
     * @throws UnsupportedOperationException always
     */
    @Override
    public Object getValue(String value) {
        throw new UnsupportedOperationException("Not supported yet."); //$NON-NLS-1$
    }

    @Override
    public boolean isNull(String value) {
        return value == null;
    }

    @Override
    public boolean supportsCompare() {
        if (basicType.isValueDatatype() && ((ValueDatatype)basicType).supportsCompare()) {
            return true;
        }
        return false;
    }

    @Override
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("The basicType " + basicType.getQualifiedName() //$NON-NLS-1$
                    + " does not support comparison for values."); //$NON-NLS-1$
        }

        return ((ValueDatatype)basicType).compare(valueA, valueB);
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        if (basicType.isValueDatatype()) {
            return ((ValueDatatype)basicType).areValuesEqual(valueA, valueB);
        }
        return ObjectUtils.equals(valueA, valueB);
    }

}
