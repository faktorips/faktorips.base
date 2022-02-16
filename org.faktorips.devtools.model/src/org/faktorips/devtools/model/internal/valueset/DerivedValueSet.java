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
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.util.DatatypeUtil;
import org.faktorips.devtools.model.valueset.IDerivedValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.Messages;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DerivedValueSet extends ValueSet implements IDerivedValueSet {

    /**
     * Creates a new value set representing all values of the datatype provided by the parent. The
     * value set contains <code>null</code> as default.
     * 
     * @param parent the parent this valueset belongs to
     * @param partId the id this part is known by by the parent
     */
    public DerivedValueSet(IValueSetOwner parent, String partId) {
        super(ValueSetType.DERIVED, parent, partId);
    }

    @Override
    public boolean containsValue(String value, IIpsProject ipsProject) {
        ValueDatatype datatype = findValueDatatype(ipsProject);
        if (datatype == null) {
            return false;
        }

        if (!datatype.isParsable(value)) {
            return false;
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

        return true;
    }

    @Override
    public IValueSet copy(IValueSetOwner newParent, String id) {
        return new DerivedValueSet(newParent, id);
    }

    @Override
    public String toShortString() {
        return getCanonicalString();
    }

    @Override
    public String getCanonicalString() {
        return Messages.ValueSetFormat_derived;
    }

    @Override
    public boolean isContainsNull() {
        if (getValueSetOwner() instanceof IPolicyCmptTypeAttribute) {
            IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)getValueSetOwner();
            if (policyCmptTypeAttribute.isOverwrite()) {
                IAttribute overwrittenAttribute = policyCmptTypeAttribute.findOverwrittenAttribute(getIpsProject());
                if (overwrittenAttribute != null) {
                    return overwrittenAttribute.getValueSet().isContainsNull();
                }
            }
        }
        ValueDatatype valueDatatype = findValueDatatype(getIpsProject());
        return valueDatatype == null || !valueDatatype.isPrimitive();
    }

    @Override
    public void setContainsNull(boolean containsNull) {
        throw new UnsupportedOperationException(Messages.DerivedValueSet_MsgCantSetContainsNull);
    }

    @Override
    public int compareTo(IValueSet o) {
        if (o.isDerived()) {
            return 0;
        } else {
            return compareDifferentValueSets(o);
        }
    }

    @Override
    protected void copyPropertiesFrom(IValueSet source) {
        // nothing to
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
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(ValueToXmlHelper.XML_TAG_DERIVED);
        element.appendChild(tagElement);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
