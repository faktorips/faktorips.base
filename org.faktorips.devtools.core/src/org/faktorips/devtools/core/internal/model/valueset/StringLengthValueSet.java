/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides a maximum number of characters for a <code>String</code> attribute. Also defines whether
 * or not <code>null</code> is a valid value for that String attribute. Other ValueSets of this type
 * are considered subsets only if they define the same or lower maximum length. A
 * <code>StringLengthValueSet</code> with maximum length of 0 and not containing <code>null</code>
 * is considered empty. A maximum length of <code>null</code> is considered to be unrestricted.
 */
public class StringLengthValueSet extends ValueSet implements IStringLengthValueSet {

    private static final String XML_TAG_STRINGLENGTH = ValueToXmlHelper.XML_TAG_STRINGLENGTH;
    private String maximumLength;
    private boolean containsNull;

    public StringLengthValueSet(IValueSetOwner parent, String partId) {
        this(parent, partId, null);
    }

    public StringLengthValueSet(IValueSetOwner parent, String partId, String maximumLength) {
        this(parent, partId, maximumLength, false);
    }

    public StringLengthValueSet(IValueSetOwner parent, String partId, String maximumLength, boolean containsNull) {
        super(ValueSetType.STRINGLENGTH, parent, partId);
        this.maximumLength = maximumLength;
        this.containsNull = containsNull;
    }

    public void setMaximumLength(String maximumLength) {
        String oldMax = this.maximumLength;
        this.maximumLength = maximumLength;
        valueChanged(oldMax, maximumLength);
    }

    @Override
    public String getMaximumLength() {
        return maximumLength;
    }

    @Override
    public boolean containsValue(String value, IIpsProject ipsProject) throws CoreException {
        return value == null ? isContainsNull() : value.length() <= Integer.parseInt(maximumLength);
    }

    @Override
    public boolean containsValueSet(IValueSet subset) {
        if (subset.isStringLength()) {
            return compareTo(subset) >= 0;
        } else if (subset.isEnum()) {
            return containsAllValues((EnumValueSet)subset);
        }
        return false;
    }

    private boolean containsAllValues(EnumValueSet subset) {
        IIpsProject contextProject = subset.getIpsProject();
        String[] subsetValues = subset.getValues();
        for (String value : subsetValues) {
            try {
                if (!containsValue(value, contextProject)) {
                    return false;
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public IValueSet copy(IValueSetOwner newParent, String id) {
        return new StringLengthValueSet(newParent, id, maximumLength, containsNull);
    }

    @Override
    protected void copyPropertiesFrom(IValueSet source) {
        StringLengthValueSet src = (StringLengthValueSet)source;
        maximumLength = src.getMaximumLength();
        containsNull = src.isContainsNull();
        objectHasChanged();
    }

    @Override
    public String toShortString() {
        return getCanonicalString();
    }

    @Override
    public String getCanonicalString() {
        String limit = getMaximumLength() == null ? Messages.StringLength_unlimitedLength : getMaximumLength();
        StringBuilder sb = new StringBuilder(NLS.bind(Messages.StringLength_canonicalDesc, limit));
        if (isContainsNull()) {
            sb.append(" (").append(NLS.bind(Messages.ValueSet_includingNull, IpsPlugin.getDefault().getIpsPreferences() //$NON-NLS-1$
                    .getNullPresentation())).append(")"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    @Override
    public boolean isContainsNull() {
        return containsNull;
    }

    @Override
    public void setContainsNull(boolean containsNull) {
        boolean old = this.isContainsNull();
        this.containsNull = containsNull;
        valueChanged(old, containsNull, PROPERTY_CONTAINS_NULL);
    }

    @Override
    public boolean isEmpty() {
        return isContainsNull() ? false : maximumLength != null && Integer.parseInt(maximumLength) == 0;
    }

    @Override
    public int compareTo(IValueSet o) {
        if (!o.isStringLength()) {
            return compareDifferentValueSets(o);
        }
        String oLength = ((IStringLengthValueSet)o).getMaximumLength();
        int equality;
        if (oLength == null && maximumLength == null) {
            equality = 0;
        } else if (oLength == null) {
            equality = -1;
        } else if (maximumLength == null) {
            equality = 1;
        } else {
            equality = Integer.compare(Integer.parseInt(maximumLength), Integer.parseInt(oLength));
        }
        return equality == 0 ? compareContainsNull(o) : equality;
    }

    private int compareContainsNull(IValueSet o) {
        if (containsNull) {
            return o.isContainsNull() ? 0 : 1;
        }
        return o.isContainsNull() ? -1 : 0;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        Element el = DescriptionHelper.getFirstNoneDescriptionElement(element);
        maximumLength = ValueToXmlHelper.getValueFromElement(el, StringUtils.capitalize(PROPERTY_MAXIMUMLENGTH));
        containsNull = Boolean.valueOf(el.getAttribute(PROPERTY_CONTAINS_NULL)).booleanValue();
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG_STRINGLENGTH);
        tagElement.setAttribute(PROPERTY_CONTAINS_NULL, Boolean.toString(isContainsNull()));
        ValueToXmlHelper.addValueToElement(maximumLength, tagElement,
                StringUtils.capitalize(PROPERTY_MAXIMUMLENGTH));
        element.appendChild(tagElement);
    }

    @Override
    protected StringLengthValueSetValidator createValidator(IValueSetOwner owner, ValueDatatype datatype) {
        return new StringLengthValueSetValidator(this, owner, datatype);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        StringLengthValueSetValidator validator = createValidator(getValueSetOwner(), findValueDatatype(ipsProject));
        list.add(validator.validate());
    }
}
