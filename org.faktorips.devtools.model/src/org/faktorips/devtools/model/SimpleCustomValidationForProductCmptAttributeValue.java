/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;

/**
 * Base class for the implementation (by way of implementing {@link #validate(String, IIpsProject)})
 * of simple custom validations for attribute values defined in product components / generations.
 * "Simple" here means that the value of a single attribute is validated. For example the value of
 * the attribute "short name" must be at most 20 characters long. For more sophisticated checks that
 * involve for example dependencies between attributes (e.g. if 'SumInsuredType' is 'Percentage'
 * then 'sumInsured' must be set), you should inherit from {@link AbstractCustomValidation}.
 * 
 * @author Jan Ortmann
 */
public abstract class SimpleCustomValidationForProductCmptAttributeValue
        extends AbstractCustomValidation<IAttributeValue> {

    private String productCmptTypeName;
    private String attributeName;

    /**
     * Creates a simple validation for the product component attribute with the given name. Note
     * that if several types have an attribute with the given name, the validation is applied to all
     * of them.
     * 
     * @param attributeName The name of the attribute
     * 
     * @throws NullPointerException if {@code attributeName} is <code>null</code>
     */
    public SimpleCustomValidationForProductCmptAttributeValue(String attributeName) {
        this(null, attributeName);
    }

    /**
     * Creates a simple validation for the given product component attribute defined by product
     * component type and attribute name.
     * 
     * @param productCmptTypeName The qualified name of the product component type
     * @param attributeName The name of the attribute
     * 
     * @throws NullPointerException if {@code attributeName} is <code>null</code>.
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
    public MessageList validate(IAttributeValue attribute, IIpsProject ipsProject) throws CoreRuntimeException {
        if (!attributeName.equals(attribute.getAttribute())) {
            return null;
        }
        if (productCmptTypeName != null
                && !productCmptTypeName.equals(attribute.getPropertyValueContainer().getProductCmptType())) {
            return null;
        }
        try {
            ValidationResult result = validate(attribute.getPropertyValue(), ipsProject);
            if (result != null) {
                MessageList messages = new MessageList();
                Message msg = new Message(result.msgCode, result.text, result.severity, attribute,
                        IAttributeValue.PROPERTY_VALUE_HOLDER);
                messages.add(msg);
                return messages;
            }
        } catch (Exception e) {
            IpsLog.log(e);
        }
        return null;
    }

    @Override
    public String toString() {
        String type = productCmptTypeName == null ? "any" : productCmptTypeName; //$NON-NLS-1$
        return "Custom validation for " + type + ". " + attributeName; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Validates the attribute's value.
     * 
     * @param value the value of the attribute
     * @param ipsProject the project containing the attribute's product component
     * @return a {@link ValidationResult} containing a message text and code as well as an error
     *         level or {@code null} if the validation finds no problems.
     * @throws CoreRuntimeException if an error occurs while validating the attribute
     */
    public abstract ValidationResult validate(String value, IIpsProject ipsProject) throws CoreRuntimeException;

    public static ValidationResult newError(String code, String text) {
        return new ValidationResult(Message.ERROR, text, code);
    }

    public static ValidationResult newWarning(String code, String text) {
        return new ValidationResult(Message.WARNING, text, code);
    }

    public static ValidationResult newInfo(String code, String text) {
        return new ValidationResult(Message.INFO, text, code);
    }

    /**
     * A simplified collection of values that will be used to create a {@link Message}.
     */
    public static class ValidationResult {

        private Severity severity;
        private String text;
        private String msgCode;

        ValidationResult(Severity warning, String text, String msgCode) {
            this.severity = warning;
            this.text = text;
            this.msgCode = msgCode;
        }

    }

}
