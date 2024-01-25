/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link ValueSet} is the specification of a set of values. It is assumed that all values in a
 * {@link ValueSet} are of the same datatype.
 * <p>
 * Values in the set are represented by strings so that we don't have to deal with type conversion
 * when the datatype changes. E.g. If an attributes datatype is changed by the user from
 * {@link Decimal} to {@link Money}, lower bound and upper bound from a range value set become
 * invalid (if they were valid before) but the string values remain. The user can switch the
 * datatype back to {@link Decimal} and the range is valid again. This works also when the
 * attribute's datatype is unknown.
 * 
 * @author Thorsten Guenther
 * @author Jan Ortmann
 */
public abstract class ValueSet extends AtomicIpsObjectPart implements IValueSet {

    /**
     * Name of the xml element used in the xml conversion.
     */
    public static final String XML_TAG = ValueToXmlHelper.XML_TAG_VALUE_SET;

    /**
     * Flag that defines this valueset as abstract
     */
    private boolean abstractFlag = false;

    /**
     * The type this value set is of.
     */
    private ValueSetType type;

    /**
     * Creates a new value set of the given type and with the given parent and id.
     * 
     * @param type The type for the new valueset.
     * @param parent The parent this valueset belongs to. Must implement {@link IValueSetOwner}.
     * @param partId The id this valueset is known by the parent.
     */
    protected ValueSet(ValueSetType type, IValueSetOwner parent, String partId) {
        super(parent, partId);
        this.type = type;
    }

    @Override
    public IValueSetOwner getValueSetOwner() {
        return (IValueSetOwner)super.getParent();
    }

    @Override
    public String getName() {
        return type.getName();
    }

    @Override
    public ValueSetType getValueSetType() {
        return type;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        String abstractString = element.getAttribute(PROPERTY_ABSTRACT);
        if (IpsStringUtils.isNotEmpty(abstractString)) {
            abstractFlag = Boolean.parseBoolean(element.getAttribute(PROPERTY_ABSTRACT));
        } else {
            /*
             * Backwards-compatibility: if no attribute "abstract" is found, abstractFlag is assumed
             * to be false. Valueset then acts exactly like a valueset up to version 2.3.x
             */
            abstractFlag = false;
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (abstractFlag) {
            element.setAttribute(PROPERTY_ABSTRACT, Boolean.toString(abstractFlag));
        }
    }

    /**
     * @param original The value to use if available (might be <code>null</code>).
     * @param alternative The value to use if the original value is <code>null</code>.
     * 
     * @return Either the original value (if not <code>null</code>) or the alternative string.
     */
    protected String getProperty(String original, String alternative) {
        if (original == null) {
            return alternative;
        }
        return original;
    }

    /**
     * Returns the datatype this value set is based on or <code>null</code>, if the datatype is not
     * provided by the parent or the datatype provided is not a <code>ValueDatatype</code>.
     * 
     * @deprecated This method may provide the wrong datatype instance because always searches in
     *                 the current project instead of a given context ips project. Use
     *                 {@link #findValueDatatype(IIpsProject)} instead.
     */
    @Deprecated
    public ValueDatatype getValueDatatype() {
        return findValueDatatype(getIpsProject());
    }

    /**
     * Returns the data type this value set is based on or <code>null</code>, if the data type is
     * not provided by the parent or the data type provided is not a <code>ValueDatatype</code>.
     */
    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) {
        return getValueSetOwner().findValueDatatype(ipsProject);
    }

    @Override
    public final void setValuesOf(IValueSet source) {
        if (source == null) {
            return;
        }
        if (!isUnrestricted()) {
            abstractFlag = source.isAbstract();
        }
        if (getValueSetType().equals(source.getValueSetType())) {
            copyPropertiesFrom(source);
        }
    }

    protected abstract void copyPropertiesFrom(IValueSet source);

    @Override
    public void setAbstract(boolean isAbstract) {
        if (isUnrestricted() && isAbstract) {
            throw new RuntimeException("Can't set an unrestricted value set to abstract!"); //$NON-NLS-1$
        }
        boolean abstractOld = isAbstract();
        abstractFlag = isAbstract;
        valueChanged(abstractOld, isAbstract());
    }

    @Override
    public boolean isAbstract() {
        return abstractFlag;
    }

    @Override
    public boolean isDetailedSpecificationOf(IValueSet otherValueSet) {
        if (otherValueSet.isUnrestricted() || otherValueSet.isDerived() || otherValueSet.isStringLength()) {
            return otherValueSet.containsValueSet(this);
        }
        if (isEnum() && otherValueSet.isRange()) {
            // this is only possible if the method names and return types are unified
            if (getIpsProject().getIpsArtefactBuilderSet().usesUnifiedValueSets()) {
                return otherValueSet.containsValueSet(this);
            }
        }
        if (!getValueSetType().equals(otherValueSet.getValueSetType())) {
            return false;
        }
        return otherValueSet.containsValueSet(this);
    }

    @Override
    public boolean isSameTypeOfValueSet(IValueSet other) {
        if (other == null) {
            return false;
        }
        return getValueSetType().equals(other.getValueSetType());
    }

    @Override
    public boolean isUnrestricted() {
        return getValueSetType().isUnrestricted();
    }

    @Override
    public boolean isEnum() {
        return getValueSetType().isEnum();
    }

    @Override
    public boolean canBeUsedAsSupersetForAnotherEnumValueSet() {
        return isEnum() && !isAbstract();
    }

    @Override
    public boolean isRange() {
        return getValueSetType().isRange();
    }

    @Override
    public boolean isAbstractAndNotUnrestricted() {
        return !isUnrestricted() && isAbstract();
    }

    /**
     * Returns <code>true</code> if the {@link ValueDatatype} is null or not a primitive datatype.
     */
    protected boolean isContainingNullAllowed(IIpsProject ipsProject) {
        ValueDatatype dataType = findValueDatatype(ipsProject);
        return dataType == null || !dataType.isPrimitive();
    }

    protected int compareDifferentValueSets(IValueSet o) {
        if (isDerived()) {
            return -2;
        } else if (o.isDerived()) {
            return 2;
        } else if (isUnrestricted() || ((isEnum() || isStringLength()) && !o.isUnrestricted())) {
            return -1;
        } else {
            return 1;
        }
    }

    protected abstract AbstractValueSetValidator<?> createValidator(IValueSetOwner owner, ValueDatatype datatype);

}
