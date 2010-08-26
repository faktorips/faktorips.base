/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumUtil;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumLiteralNameAttribute</tt>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class EnumLiteralNameAttribute extends EnumAttribute implements IEnumLiteralNameAttribute {

    /**
     * The name of the <tt>IEnumAttribute</tt> that is used as default value provider for
     * enumeration literals.
     */
    private String defaultValueProviderAttribute;

    /**
     * Creates a new <tt>IEnumLiteralNameAttribute</tt>.
     * 
     * @param parent The <tt>IEnumType</tt> this <tt>IEnumLiteralNameAttribute</tt> belongs to.
     * @param id A unique ID for this <tt>IEnumLiteralNameAttribute</tt>.
     */
    public EnumLiteralNameAttribute(IEnumType parent, String id) {
        super(parent, id);
        defaultValueProviderAttribute = ""; //$NON-NLS-1$
    }

    @Override
    public void setDefaultValueProviderAttribute(String defaultValueProviderAttributeName) {
        ArgumentCheck.notNull(defaultValueProviderAttributeName);

        String oldValue = defaultValueProviderAttribute;
        defaultValueProviderAttribute = defaultValueProviderAttributeName;
        valueChanged(oldValue, defaultValueProviderAttributeName);
    }

    @Override
    public String getDefaultValueProviderAttribute() {
        return defaultValueProviderAttribute;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        defaultValueProviderAttribute = element.getAttribute(PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE, defaultValueProviderAttribute);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateIsNeeded(list);
        if (list.getNoOfMessages() == 0) {
            validateDefaultValueProviderAttribute(list, ipsProject);
        }
    }

    /**
     * Validates whether this <tt>IEnumLiteralNameAttribute</tt> is needed by the <tt>IEnumType</tt>
     * it belongs to.
     */
    private void validateIsNeeded(MessageList list) throws CoreException {
        IEnumType enumType = getEnumType();
        if (!(enumType.isCapableOfContainingValues())) {
            String text = Messages.EnumLiteralNameAttribute_NotNeeded;
            Message msg = new Message(MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED, text, Message.ERROR, this);
            list.add(msg);
        }
    }

    /** Validates the <tt>defaultValueProviderAttribute</tt> property. */
    private void validateDefaultValueProviderAttribute(MessageList list, IIpsProject ipsProject) throws CoreException {
        // Pass validation if no provider is specified.
        if (defaultValueProviderAttribute.length() == 0) {
            return;
        }

        // The provider attribute must exist in the parent EnumType.
        IEnumType enumType = getEnumType();
        if (!(enumType.containsEnumAttributeIncludeSupertypeCopies(defaultValueProviderAttribute))) {
            String text = NLS.bind(Messages.EnumLiteralNameAttribute_DefaultValueProviderAttributeDoesNotExist,
                    defaultValueProviderAttribute);
            Message msg = new Message(
                    MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST, text,
                    Message.ERROR, this, PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
            list.add(msg);
            return;
        }

        // The provider attribute must be of data type String.
        IEnumAttribute providerAttribute = enumType
                .getEnumAttributeIncludeSupertypeCopies(defaultValueProviderAttribute);
        Datatype datatype = providerAttribute.findDatatype(getIpsProject());
        if (datatype != null) {
            if (!(datatype.equals(Datatype.STRING))) {
                String text = NLS.bind(
                        Messages.EnumLiteralNameAttribute_DefaultValueProviderAttributeNotOfDatatypeString,
                        defaultValueProviderAttribute);
                Message msg = new Message(
                        MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING,
                        text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
                list.add(msg);
            }
        }

        // The provider attribute must be unique.
        if (!(EnumUtil.findEnumAttributeIsUnique(providerAttribute, ipsProject))) {
            String text = NLS.bind(Messages.EnumLiteralNameAttribute_DefaultValueProviderAttributeNotUnique,
                    defaultValueProviderAttribute);
            Message msg = new Message(MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_UNIQUE,
                    text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
            list.add(msg);
        }
    }

    /**
     * Not supported by <tt>IEnumLiteralNameAttribute</tt>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setIdentifier(boolean usedAsIdInFaktorIpsUi) {
        throw new UnsupportedOperationException("The identifier property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    /**
     * Not supported by <tt>IEnumLiteralNameAttribute</tt>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setInherited(boolean isInherited) {
        throw new UnsupportedOperationException("The inherited property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    /**
     * Not supported by <tt>IEnumLiteralNameAttribute</tt>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setUsedAsNameInFaktorIpsUi(boolean usedAsNameInFaktorIpsUi) {
        throw new UnsupportedOperationException("The usedAsName property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IEnumLiteralNameAttribute.XML_TAG);
    }

    @Override
    public ProcessorBasedRefactoring getRenameRefactoring() {
        return null;
    }

}
