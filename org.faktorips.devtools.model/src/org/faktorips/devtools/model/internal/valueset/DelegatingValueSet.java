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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IDelegatingValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link DelegatingValueSet} is a value set implementation that delegates every reading call to
 * another value set. Depending on the delegate target it acts as {@link IEnumValueSet},
 * {@link IRangeValueSet} or {@link IUnrestrictedValueSet}.
 * 
 * Every writing call leads to an {@link IllegalStateException}
 */
public class DelegatingValueSet extends AtomicIpsObjectPart implements IDelegatingValueSet {

    private final ValueSet delegate;

    /**
     * Creates a new value set of the given type and with the given parent.
     *
     * @param delegate The original value set
     * @param parent The parent this valueset belongs to. Must implement IValueDatatypeProvider.
     */
    public DelegatingValueSet(ValueSet delegate, IValueSetOwner parent) {
        super(parent, StringUtils.EMPTY);
        this.delegate = delegate;
    }

    @Override
    public IValueSetOwner getValueSetOwner() {
        return (IValueSetOwner)super.getParent();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public ValueSetType getValueSetType() {
        return delegate.getValueSetType();
    }

    @Override
    protected Element createElement(Document doc) {
        return delegate.createElement(doc);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        // ignore - may be called by reload of already initialized parts
    }

    @Override
    protected void propertiesToXml(Element element) {
        delegate.propertiesToXml(element);
    }

    @Override
    public final void setValuesOf(IValueSet source) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public void setAbstract(boolean isAbstract) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public boolean isAbstract() {
        return delegate.isAbstract();
    }

    @Override
    public boolean isDetailedSpecificationOf(IValueSet otherValueSet) {
        return delegate.isDetailedSpecificationOf(otherValueSet);
    }

    @Override
    public boolean isSameTypeOfValueSet(IValueSet other) {
        return delegate.isSameTypeOfValueSet(other);
    }

    @Override
    public boolean isUnrestricted() {
        return delegate.isUnrestricted();
    }

    @Override
    public boolean isEnum() {
        return delegate.isEnum();
    }

    @Override
    public boolean canBeUsedAsSupersetForAnotherEnumValueSet() {
        return delegate.canBeUsedAsSupersetForAnotherEnumValueSet();
    }

    @Override
    public boolean isRange() {
        return delegate.isRange();
    }

    @Override
    public boolean isAbstractAndNotUnrestricted() {
        return delegate.isAbstractAndNotUnrestricted();
    }

    @Override
    public boolean containsValue(String value, IIpsProject ipsProject) {
        return delegate.containsValue(value, ipsProject);
    }

    @Override
    public boolean containsValueSet(IValueSet subset) {
        return delegate.containsValueSet(subset);
    }

    @Override
    public IValueSet copy(IValueSetOwner newParent, String id) {
        return delegate.copy(newParent, id);
    }

    @Override
    public String toShortString() {
        return delegate.toShortString();
    }

    @Override
    public String getCanonicalString() {
        return delegate.getCanonicalString();
    }

    @Override
    public boolean isContainsNull() {
        return delegate.isContainsNull();
    }

    @Override
    public void setContainsNull(boolean containsNull) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$

    }

    @Override
    public void setLowerBound(String lowerBound) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public void setStep(String step) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public void setUpperBound(String upperBound) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    private IRangeValueSet getRangeDelegate() {
        if (delegate instanceof IRangeValueSet) {
            return (IRangeValueSet)delegate;
        } else {
            throw new IllegalStateException("DelegateValueSets is not a range"); //$NON-NLS-1$
        }
    }

    @Override
    public String getLowerBound() {
        return getRangeDelegate().getLowerBound();
    }

    @Override
    public String getUpperBound() {
        return getRangeDelegate().getUpperBound();
    }

    @Override
    public String getStep() {
        return getRangeDelegate().getStep();
    }

    private EnumValueSet getEnumDelegate() {
        if (delegate instanceof EnumValueSet) {
            return (EnumValueSet)delegate;
        } else {
            throw new IllegalStateException("DelegateValueSets is not a enum"); //$NON-NLS-1$
        }
    }

    @Override
    public String[] getValues() {
        return getEnumDelegate().getValues();
    }

    @Override
    public List<Integer> getPositions(String value) {
        return getEnumDelegate().getPositions(value);
    }

    @Override
    public void addValue(String val) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public void addValues(List<String> values) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public void removeValue(int index) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public void removeValue(String string) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public void removeValues(List<String> values) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public String getValue(int index) {
        return getEnumDelegate().getValue(index);
    }

    @Override
    public void setValue(int index, String value) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public int size() {
        return getEnumDelegate().size();
    }

    @Override
    public String[] getValuesNotContained(IEnumValueSet otherSet) {
        return getEnumDelegate().getValuesNotContained(otherSet);
    }

    @Override
    public void addValuesFromDatatype(EnumDatatype datatype) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public List<String> getValuesAsList() {
        return getEnumDelegate().getValuesAsList();
    }

    @Override
    public void move(List<Integer> indexes, boolean up) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    public void move(List<Integer> indices, int targetIndex, boolean insertBelow) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        AbstractValueSetValidator<?> validator = delegate.createValidator(getValueSetOwner(),
                getValueSetOwner().findValueDatatype(ipsProject));
        list.add(validator.validate());
    }

    @Override
    public MessageList validateValue(int index, IIpsProject ipsProject) {
        EnumValueSetValidator validator = getEnumDelegate().createValidator(getValueSetOwner(),
                findValueDatatype(ipsProject));
        return validator.validateValue(index);
    }

    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) {
        return delegate.findValueDatatype(ipsProject);
    }

    @Override
    public int compareTo(IValueSet o) {
        return delegate.compareTo(o);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public void setEmpty(boolean empty) {
        throw new IllegalStateException("DelegateValueSets cannot be changed"); //$NON-NLS-1$
    }
}
