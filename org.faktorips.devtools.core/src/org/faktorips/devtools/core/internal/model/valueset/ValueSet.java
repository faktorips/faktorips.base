/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A ValueSet is the specification of a set of values. It is asumed that all values in a ValueSet
 * are of the same datatype.
 * <p>
 * Values in the set are represented by strings so that we don't have to deal with type conversion
 * when the datatype changes. E.g. If an attributes datatype is changed by the user from Decimal to
 * Money, lower bound and upper bound from a range value set become invalid (if they were valid
 * before) but the string values remain. The user can switch back the datatype to Decimal and the
 * range is valid again. This works also when the attribute's datatype is unknown.
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
     * @param parent The parent this valueset belongs to. Must implement IValueDatatypeProvider.
     * @param partId The id this valueset is known by the parent.
     * 
     * @throws IllegalArgumentException if the parent does not implement the interface
     *             <code>IValueDatatypeProvider</code>.
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
        if (StringUtils.isNotEmpty(abstractString)) {
            abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT));
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
        element.setAttribute(PROPERTY_ABSTRACT, Boolean.toString(abstractFlag));
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
     */
    public ValueDatatype getValueDatatype() {
        try {
            // TODO getIpsProject() needs to be replaced with a paramter!
            return ((IValueSetOwner)parent).findValueDatatype(getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the data type this value set is based on or <code>null</code>, if the data type is
     * not provided by the parent or the data type provided is not a <code>ValueDatatype</code>.
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException {
        return ((IValueSetOwner)parent).findValueDatatype(ipsProject);
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
        if (otherValueSet.isUnrestricted()) {
            return true;
        }
        if (!getValueSetType().equals(otherValueSet.getValueSetType())) {
            return false;
        }
        if (otherValueSet.isAbstract()) {
            return true;
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
        return getValueSetType() == ValueSetType.UNRESTRICTED;
    }

    @Override
    public boolean isEnum() {
        return getValueSetType() == ValueSetType.ENUM;
    }

    @Override
    public boolean canBeUsedAsSupersetForAnotherEnumValueSet() {
        return isEnum() && !isAbstract();
    }

    @Override
    public boolean isRange() {
        return getValueSetType() == ValueSetType.RANGE;
    }

    @Override
    public boolean isAbstractAndNotUnrestricted() {
        return !isUnrestricted() && isAbstract();
    }

    /**
     * @deprecated Use {@link #isContainsNull()} instead
     */
    @Deprecated
    @Override
    public final boolean isContainingNull() {
        return isContainsNull();
    }

    /**
     * Creates a new message with severity ERROR and adds the new message to the given message list.
     * 
     * @param list The message list to add the new message to
     * @param id The message code
     * @param text The message text
     * @param invalidObject The object this message is for. Can be null if no relation to an object
     *            exists.
     * @param invalidProperty The name of the property the message is created for. Can be null.
     */
    protected void addMsg(MessageList list, String id, String text, Object invalidObject, String invalidProperty) {
        addMsg(list, Message.ERROR, id, text, invalidObject, invalidProperty);
    }

    /**
     * Creates a new message with severity ERROR and adds the new message to the given message list.
     * 
     * @param list The message list to add the new message to
     * @param id The message code
     * @param text The message text
     * @param invalidObject The object this message is for. Can be null if no relation to an object
     *            exists.
     * @param invalidProperty The name of the property the message is created for. Can be null.
     */
    protected void addMsg(MessageList list,
            int severity,
            String id,
            String text,
            Object invalidObject,
            String invalidProperty) {
        ObjectProperty parentObjectProperty = new ObjectProperty(getValueSetOwner(), IValueSetOwner.PROPERTY_VALUE_SET);
        ObjectProperty invalidObjectProperty = new ObjectProperty(invalidObject != null ? invalidObject : this,
                invalidProperty);
        Message msg = new Message(id, text, severity, parentObjectProperty, invalidObjectProperty);
        list.add(msg);
    }

    protected boolean isNullValue(String value, ValueDatatype datatype) {
        return value == null || datatype.isNull(value);
    }

}
