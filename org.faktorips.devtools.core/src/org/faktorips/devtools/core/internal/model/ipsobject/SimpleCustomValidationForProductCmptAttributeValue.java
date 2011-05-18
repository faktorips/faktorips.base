/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A class that allows to implement simple custom validations for attribute values defined in
 * product component generations. By simple we mean that the value of a single attribute is
 * validated. E.g. the value of the attribute "short name" must be at least 20 characters long. For
 * more sophisticated checks that involves for example dependencies between attributes (e.g. if
 * 'SumInsuredType' is 'Percentage' then 'sumInsured' must be set), you should inherit from
 * {@link AbstractCustomValidation}.
 * 
 * @author Jan Ortmann
 */
public abstract class SimpleCustomValidationForProductCmptAttributeValue extends
        AbstractCustomValidation<IAttributeValue> {

    private String productCmptTypeName;
    private String attributeName;

    /**
     * Creates a simple validation for the product component attribute with the given name. Note
     * that if severall types have an attribute with the given name, the validation is applied to
     * all of them.
     * 
     * @param attributeName The name of the attribute.
     * 
     * @throws NullPointerException if attributeName is <code>null</code>.
     */
    public SimpleCustomValidationForProductCmptAttributeValue(String attributeName) {
        this(null, attributeName);
    }

    /**
     * Creates a simple validation for the given product component attribute defined by product
     * component type and attribute name.
     * 
     * @param productCmptTypeName The qualified name of the product component type.
     * @param attributeName The name of the attribute.
     * 
     * @throws NullPointerException if attributeName is <code>null</code>.
     */
    public SimpleCustomValidationForProductCmptAttributeValue(String productCmptTypeName, String attributeName) {
        super(IAttributeValue.class);
        this.productCmptTypeName = productCmptTypeName;
        this.attributeName = attributeName;
    }

    public String getProductCmptTypeName() {
        return productCmptTypeName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public MessageList validate(IAttributeValue attribute, IIpsProject ipsProject) throws CoreException {
        if (!attributeName.equals(attribute.getAttribute())) {
            return null;
        }
        if (productCmptTypeName != null
                && !productCmptTypeName.equals(attribute.getPropertyValueContainer().getProductCmptType())) {
            return null;
        }
        try {
            ValidationResult result = validate(attribute.getValue(), ipsProject);
            if (result != null) {
                MessageList messages = new MessageList();
                Message msg = new Message(result.msgCode, result.text, result.severity, attribute,
                        IAttributeValue.PROPERTY_VALUE);
                messages.add(msg);
                return messages;
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @Override
    public String toString() {
        String type = productCmptTypeName == null ? "any" : productCmptTypeName; //$NON-NLS-1$
        return "Custom validation for " + type + ". " + attributeName; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public abstract ValidationResult validate(String value, IIpsProject ipsProject) throws CoreException;

    public static ValidationResult newError(String code, String text) {
        return new ValidationResult(Message.ERROR, text, code);
    }

    public static ValidationResult newWarning(String code, String text) {
        return new ValidationResult(Message.WARNING, text, code);
    }

    public static ValidationResult newInfo(String code, String text) {
        return new ValidationResult(Message.INFO, text, code);
    }

    public static class ValidationResult {

        private int severity;
        private String text;
        private String msgCode;

        ValidationResult(int severity, String text, String msgCode) {
            this.severity = severity;
            this.text = text;
            this.msgCode = msgCode;
        }

    }

}
