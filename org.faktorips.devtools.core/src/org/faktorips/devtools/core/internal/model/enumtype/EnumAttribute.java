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
import org.faktorips.devtools.core.IpsPlugin;
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

    // The icon representing an enum attribute
    private final String ICON = "EnumAttribute.gif";

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

        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
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

        String oldDatatype = this.datatype;
        this.datatype = datatype;
        valueChanged(oldDatatype, datatype);
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
    public void setIdentifier(boolean isIdentifier) {
        boolean oldIsIdentifier = this.isIdentifier;
        this.isIdentifier = isIdentifier;
        valueChanged(oldIsIdentifier, isIdentifier);
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
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        isIdentifier = Boolean.parseBoolean(element.getAttribute(PROPERTY_IDENTIFIER));

        super.initFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_IDENTIFIER, String.valueOf(isIdentifier));
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage(ICON); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        String text;
        Message validationMessage;
        List<IEnumAttribute> enumAttributesThisType = ((IEnumType)getParent()).getEnumAttributes();

        // Check for name missing
        if (name.equals("")) {
            text = Messages.EnumAttribute_NameMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING, text, Message.ERROR, this,
                    PROPERTY_NAME);
            list.add(validationMessage);
        }

        // Check for other attributes with the same name
        int numberEnumAttributesThisName = 0;
        for (IEnumAttribute currentEnumAttribute : enumAttributesThisType) {
            if (currentEnumAttribute.getName().equals(name)) {
                numberEnumAttributesThisName++;
            }
            if (numberEnumAttributesThisName > 1) {
                text = NLS.bind(Messages.EnumAttribute_DuplicateName, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME, text, Message.ERROR, this,
                        PROPERTY_NAME);
                list.add(validationMessage);
                break;
            }
        }

        // Check for missing datatype or datatype not existing
        if (datatype.equals("")) {
            text = Messages.EnumAttribute_DatatypeMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
        } else {
            if (getIpsProject().findDatatype(datatype) == null) {
                text = NLS.bind(Messages.EnumAttribute_DatatypeDoesNotExist, datatype);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST, text, Message.ERROR,
                        this, PROPERTY_DATATYPE);
                list.add(validationMessage);
            }
        }

        // Check for other attributes being marked as identifier
        if (isIdentifier) {
            int numberEnumAttributesIdentifier = 0;
            for (IEnumAttribute currentEnumAttribute : enumAttributesThisType) {
                if (currentEnumAttribute.isIdentifier()) {
                    numberEnumAttributesIdentifier++;
                }
                if (numberEnumAttributesIdentifier > 1) {
                    text = Messages.EnumAttribute_DuplicateIdentifier;
                    validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_IDENTIFIER, text, Message.ERROR,
                            this, PROPERTY_IDENTIFIER);
                    list.add(validationMessage);
                    break;
                }
            }
        }
    }

}
