/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.type;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

public class AttributeAbstractDatatypeValidator {

    private final IType type;

    private final IAttribute attribute;

    private IIpsProject ipsProject;

    public AttributeAbstractDatatypeValidator(IAttribute attribute, IIpsProject ipsProject) {
        this(attribute, attribute.getType(), ipsProject);
    }

    public AttributeAbstractDatatypeValidator(IAttribute attribute, IType type, IIpsProject ipsProject) {
        this.attribute = attribute;
        this.type = type;
        this.ipsProject = ipsProject;
    }

    public void validateNotAbstractDatatype(MessageList list) {
        ValueDatatype datatype = attribute.findDatatype(ipsProject);
        if (datatype != null && datatype.isAbstract()) {
            String text = NLS.bind(Messages.AttributeAbstractDatatypeValidator_msg, attribute.getName());
            ObjectProperty[] invalidObjects;
            if (!attribute.isOfType(getQualifiedNameType())) {
                text += Messages.AttributeAbstractDatatypeValidator_hint;
                invalidObjects = new ObjectProperty[] { getAbstractProperty() };
            } else {
                invalidObjects = new ObjectProperty[] { getDatatypeProperty(), getAbstractProperty() };
            }

            list.newError(IType.MSGCODE_ABSTRACT_MISSING, text, invalidObjects);
        }
    }

    private ObjectProperty getDatatypeProperty() {
        return new ObjectProperty(attribute, IAttribute.PROPERTY_DATATYPE);
    }

    private ObjectProperty getAbstractProperty() {
        return new ObjectProperty(type, IType.PROPERTY_ABSTRACT);
    }

    private QualifiedNameType getQualifiedNameType() {
        return type.getQualifiedNameType();
    }
}
