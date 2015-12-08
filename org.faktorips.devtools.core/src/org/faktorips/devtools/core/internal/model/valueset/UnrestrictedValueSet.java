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

import static org.faktorips.devtools.core.model.DatatypeUtil.isNullValue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.DatatypeUtil;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.Messages;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.MessageList;
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
     * parent therefore has to implement IValueDatatypeProvider. The value set contains
     * <code>null</code> as default.
     * 
     * @param parent The parent this valueset belongs to.
     * @param partId The id this part is known by by the parent.
     * 
     * @throws IllegalArgumentException if the parent does not implement the interface
     *             <code>IValueDatatypeProvider</code>.
     */
    public UnrestrictedValueSet(IValueSetOwner parent, String partId) {
        super(ValueSetType.UNRESTRICTED, parent, partId);
    }

    /**
     * Creates a new value set representing all values of the datatype provided by the parent. The
     * parent therefore has to implement IValueDatatypeProvider.
     * 
     * @param parent The parent this valueset belongs to.
     * @param partId The id this part is known by by the parent.
     * @param containsNull This indicates whether this value set contains null.
     * 
     * @throws IllegalArgumentException if the parent does not implement the interface
     *             <code>IValueDatatypeProvider</code>.
     * 
     */
    public UnrestrictedValueSet(IValueSetOwner parent, String partId, boolean containsNull) {
        super(ValueSetType.UNRESTRICTED, parent, partId);
        this.containsNull = containsNull;
    }

    @Override
    public String toShortString() {
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
    public boolean containsValue(String value, IIpsProject ipsProject) throws CoreException {

        ValueDatatype datatype = findValueDatatype(ipsProject);
        if (datatype == null) {
            return false;
        }

        if (!datatype.isParsable(value)) {
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
        ValueDatatype subDatatype = ((ValueSet)subset).findValueDatatype(contextProject);

        if (!DatatypeUtil.isCovariant(subDatatype, datatype)) {
            return false;
        }

        if (!isContainsNull() && subset.isContainsNull()) {
            return false;
        }

        return true;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        Element el = DescriptionHelper.getFirstNoneDescriptionElement(element);
        if (el.hasAttribute(PROPERTY_CONTAINS_NULL)) {
            containsNull = Boolean.valueOf(el.getAttribute(PROPERTY_CONTAINS_NULL)).booleanValue();
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
        UnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(parent, id, isContainsNull());
        return unrestrictedValueSet;
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
        boolean oldContainsNull = this.isContainsNull();
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

}
