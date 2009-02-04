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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.enumtype.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IEnumValue, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValue extends BaseIpsObjectPart implements IEnumValue {

    // Collection containing all enum attribute values that belong to this EnumValue
    private IpsObjectPartCollection enumAttributeValues;

    /**
     * Creates a new enum value that has as many enum attribute values as the corresponding enum
     * type has attributes.
     * 
     * @param parent The enum value container this enum value belongs to.
     * @param id A unique id for this enum value.
     * 
     * @throws CoreException If an error occurs while initializing the enum attribute values.
     */
    public EnumValue(IEnumValueContainer parent, int id) throws CoreException {
        super(parent, id);

        this.enumAttributeValues = new IpsObjectPartCollection(this, EnumAttributeValue.class,
                IEnumAttributeValue.class, IEnumAttributeValue.XML_TAG);

        // Add as many enum attribute values as there are enum attributes in the enum type
        IEnumType enumType = parent.findEnumType();
        for (int i = 0; i < enumType.getEnumAttributes().size(); i++) {
            newEnumAttributeValue();
        }
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
    public Image getImage() {
        // TODO Image handling
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<IEnumAttributeValue> getEnumAttributeValues() {
        List<IEnumAttributeValue> attributeValuesList = new ArrayList<IEnumAttributeValue>();
        IIpsObjectPart[] parts = enumAttributeValues.getParts();
        for (IIpsObjectPart currentObjectPart : parts) {
            attributeValuesList.add((IEnumAttributeValue)currentObjectPart);
        }

        return attributeValuesList;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttributeValue getEnumAttributeValue(int id) {
        return (IEnumAttributeValue)enumAttributeValues.getPartById(id);
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttributeValue newEnumAttributeValue() throws CoreException {
        if (((IEnumValueContainer)getParent()).findEnumType().getNumberEnumAttributes() <= enumAttributeValues.size()) {
            throw new IllegalStateException("There are already as many enum attribute values as enum attributes.");
        }

        return (IEnumAttributeValue)newPart(IEnumAttributeValue.class);
    }

    /**
     * {@inheritDoc}
     */
    public void moveEnumAttributeValueDown(IEnumAttribute enumAttribute) {
        ArgumentCheck.notNull(enumAttribute);

        moveEnumAttributeValue(enumAttribute, false);
    }

    /**
     * {@inheritDoc}
     */
    public void moveEnumAttributeValueUp(IEnumAttribute enumAttribute) {
        ArgumentCheck.notNull(enumAttribute);

        moveEnumAttributeValue(enumAttribute, true);
    }

    // Moves the enum attribute value refering to the given enum attribute up or down in the
    // collection order by 1
    private void moveEnumAttributeValue(IEnumAttribute enumAttribute, boolean up) {
        List<IEnumAttributeValue> enumAttributeValuesList = getEnumAttributeValues();
        for (int i = 0; i < enumAttributeValuesList.size(); i++) {
            IEnumAttributeValue currentEnumAttributeValue = enumAttributeValuesList.get(i);
            if (currentEnumAttributeValue.getEnumAttribute() == enumAttribute) {

                // Return if element is already the first / last one
                if (up) {
                    if (i == 0) {
                        return;
                    }
                } else {
                    if (i == enumAttributeValuesList.size() - 1) {
                        return;
                    }
                }

                enumAttributeValues.moveParts(new int[] { i }, up);
                break;

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        IEnumType enumType = ((IEnumValueContainer)getParent()).findEnumType();
        if (enumType != null) {
            if (enumType.getNumberEnumAttributes() != getNumberEnumAttributeValues()) {
                String text = NLS.bind(Messages.EnumValue_NumberAttributeValuesDoesNotCorrespondToNumberAttributes,
                        enumType.getQualifiedName());
                Message message = new Message(MSGCODE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES,
                        text, Message.ERROR, this);
                list.add(message);
            }
        }
    }

    // Returns the number of enum attribute values in this enum value
    private int getNumberEnumAttributeValues() {
        return enumAttributeValues.size();
    }

}
