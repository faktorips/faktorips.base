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

import static org.faktorips.devtools.model.util.DatatypeUtil.isNullValue;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.ipsobject.DescriptionHelper;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.DatatypeUtil;
import org.faktorips.devtools.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.Messages;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IUnrestrictedValueSet
 *
 * @author Thorsten Guenther
 */
public class UnrestrictedValueSet extends ValueSet implements IUnrestrictedValueSet {

    public static final String XML_TAG_UNRESTRICTED = ValueToXmlHelper.XML_TAG_ALL_VALUES;

    /** Indicating whether this {@link UnrestrictedValueSet} contains null. */
    private boolean containsNull = true;

    /**
     * Creates a new value set representing all values of the datatype provided by the parent. The
     * value set contains <code>null</code> as default.
     *
     * @param parent the parent this valueset belongs to
     * @param partId the id this part is known by by the parent
     */
    public UnrestrictedValueSet(IValueSetOwner parent, String partId) {
        super(ValueSetType.UNRESTRICTED, parent, partId);
    }

    /**
     * Creates a new value set representing all values of the datatype provided by the parent.
     *
     * @param parent the parent this valueset belongs to
     * @param partId the id this part is known by by the parent
     * @param containsNull indicates whether this value set contains {@code null}
     */
    public UnrestrictedValueSet(IValueSetOwner parent, String partId, boolean containsNull) {
        super(ValueSetType.UNRESTRICTED, parent, partId);
        this.containsNull = containsNull;
    }

    @Override
    public String toShortString() {
        return getCanonicalString();
    }

    @Override
    public String getCanonicalString() {
        if (isContainsNull()) {
            return Messages.ValueSetFormat_unrestricted;
        } else {
            return Messages.ValueSet_unrestrictedWithoutNull;
        }
    }

    @Override
    public String toString() {
        return super.toString() + ":" + toShortString(); //$NON-NLS-1$
    }

    @Override
    public boolean containsValue(String value, IIpsProject ipsProject) {

        ValueDatatype datatype = findValueDatatype(ipsProject);
        if ((datatype == null) || !datatype.isParsable(value)) {
            return false;
        }

        if (isNullValue(datatype, value)) {
            return isContainsNull();
        }

        return true;
    }

    @Override
    public boolean containsValueSet(IValueSet subset) {
        IIpsProject contextProject = subset.getIpsProject();
        ValueDatatype datatype = findValueDatatype(contextProject);
        ValueDatatype subDatatype = subset.findValueDatatype(contextProject);

        if (!DatatypeUtil.isCovariant(subDatatype, datatype)) {
            return false;
        }

        return isContainsNull() || !subset.isContainsNull() || subset.isDerived();
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        Element el = DescriptionHelper.getFirstNoneDescriptionElement(element);
        if (el.hasAttribute(PROPERTY_CONTAINS_NULL)) {
            containsNull = ValueToXmlHelper.isAttributeTrue(el, PROPERTY_CONTAINS_NULL);
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG_UNRESTRICTED);
        tagElement.setAttribute(PROPERTY_CONTAINS_NULL, Boolean.toString(isContainsNull()));
        element.appendChild(tagElement);
    }

    @Override
    public IValueSet copy(IValueSetOwner parent, String id) {
        return new UnrestrictedValueSet(parent, id, isContainsNull());
    }

    @Override
    public void copyPropertiesFrom(IValueSet target) {
        containsNull = target.isContainsNull();
        objectHasChanged();
    }

    @Override
    public boolean isContainsNull() {
        return containsNull && isContainingNullAllowed(getIpsProject());
    }

    @Override
    public void setContainsNull(boolean containsNull) {
        boolean oldContainsNull = isContainsNull();
        this.containsNull = containsNull;
        valueChanged(oldContainsNull, containsNull, PROPERTY_CONTAINS_NULL);
    }

    @Override
    protected AbstractValueSetValidator<?> createValidator(IValueSetOwner owner, ValueDatatype datatype) {
        return new AbstractValueSetValidator<ValueSet>(this, owner, datatype) {

            @Override
            public MessageList validate() {
                return new MessageList();
            }
        };
    }

    @Override
    public int compareTo(IValueSet o) {
        if (o.isUnrestricted()) {
            if (isContainsNull() == o.isContainsNull()) {
                return 0;
            } else if (isContainsNull()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return compareDifferentValueSets(o);
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
