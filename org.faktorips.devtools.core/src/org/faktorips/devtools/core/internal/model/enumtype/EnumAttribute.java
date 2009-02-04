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
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IEnumAttribute, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttribute extends AtomicIpsObjectPart implements IEnumAttribute {

    // The datatype of this attribute
    private String datatype;

    // Flag indicating whether this attribute is an identifier
    private boolean isIdentifier;

    /**
     * Creates a new enum attribute.
     * 
     * @param parent The enum type this enum attribute belongs to.
     * @param id A unique id for this enum attribute.
     */
    public EnumAttribute(IEnumType parent, int id) {
        super(parent, id);

        this.datatype = "";
        this.isIdentifier = false;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        ArgumentCheck.notNull(name);
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String datatype) {
        ArgumentCheck.notNull(datatype);
        this.datatype = datatype;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isIdentifier() {
        return isIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    public void setIsIdentifier(boolean isIdentifier) {
        this.isIdentifier = isIdentifier;
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
    protected void initFromXml(Element element, Integer id) {
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        isIdentifier = Boolean.parseBoolean(element.getAttribute(PROPERTY_IS_IDENTIFIER));

        super.initFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_IS_IDENTIFIER, String.valueOf(isIdentifier));
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
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        String text;
        Message message;

        if (name.equals("")) {
            text = Messages.EnumAttribute_NameMissing;
            message = new Message(MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING, text, Message.ERROR, this, PROPERTY_NAME);
            list.add(message);
        }

        List<IEnumAttribute> enumAttributes = ((IEnumType)getParent()).getEnumAttributes();
        int numberEnumAttributesThisName = 0;
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (currentEnumAttribute.getName().equals(name)) {
                numberEnumAttributesThisName++;
            }
            if (numberEnumAttributesThisName > 1) {
                text = NLS.bind(Messages.EnumAttribute_DuplicateName, name);
                message = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME, text, Message.ERROR, this, PROPERTY_NAME);
                list.add(message);
                break;
            }
        }

        if (datatype.equals("")) {
            text = Messages.EnumAttribute_DatatypeMissing;
            message = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING, text, Message.ERROR, this, PROPERTY_DATATYPE);
            list.add(message);
        } else {
            if (getIpsProject().findDatatype(datatype) == null) {
                text = NLS.bind(Messages.EnumAttribute_DatatypeDoesNotExist, datatype);
                message = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                        PROPERTY_DATATYPE);
                list.add(message);
            }
        }
    }

}
