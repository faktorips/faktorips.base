/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enumtype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.enumtype.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IEnumAttributeValue, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributeValue extends AtomicIpsObjectPart implements IEnumAttributeValue {

    // The actual value
    private String value;

    /**
     * Creates a new enum attribute value.
     * 
     * @param parent The enum value this enum attribute value belongs to.
     * @param id A unique id for this enum attribute value.
     * 
     * @throws CoreException If an error occurs while initializing the object.
     */
    public EnumAttributeValue(IEnumValue parent, int id) throws CoreException {
        super(parent, id);

        this.value = "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_VALUE, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initFromXml(Element element, Integer id) {
        value = element.getAttribute(PROPERTY_VALUE);

        super.initFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttribute findEnumAttribute() throws CoreException {
        IEnumValueContainer valueContainer = (IEnumValueContainer)parent.getParent();

        // Calculate index
        int index;
        List<IEnumAttributeValue> enumAttributeValuesList = ((IEnumValue)parent).getEnumAttributeValues();
        for (index = 0; index < enumAttributeValuesList.size(); index++) {
            IEnumAttributeValue currentEnumAttributeValue = enumAttributeValuesList.get(index);
            if (currentEnumAttributeValue == this) {
                break;
            }
        }

        IEnumType enumType = valueContainer.findEnumType();
        if (enumType == null) {
            return null;
        }

        return enumType.getEnumAttributes().get(index);
    }

    /**
     * {@inheritDoc}
     */
    public String getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String value) {
        ArgumentCheck.notNull(value);

        String oldValue = this.value;
        this.value = value;
        valueChanged(oldValue, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        // Value parsable?
        IEnumAttribute enumAttribute = findEnumAttribute();
        if (enumAttribute != null) {
            String datatypeQualifiedName = enumAttribute.getDatatype();
            ValueDatatype valueDatatype = ipsProject.findValueDatatype(datatypeQualifiedName);
            if (!(valueDatatype.isParsable(value))) {
                String text = NLS.bind(Messages.EnumAttributeValue_NotParsable, enumAttribute.getName(), valueDatatype
                        .getName());
                Message validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_NOT_PARSABLE, text, Message.ERROR,
                        this, PROPERTY_VALUE);
                list.add(validationMessage);
            }
        }
    }

}
