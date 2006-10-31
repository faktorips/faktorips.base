/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FormulaTestInputValue extends IpsObjectPart implements IFormulaTestInputValue {

    /** Tags */
    final static String TAG_NAME = "FormulaTestInputValue"; //$NON-NLS-1$
    
    private String identifier = ""; //$NON-NLS-1$
    private String value = ""; //$NON-NLS-1$
    
    private boolean deleted = false;
    
    public FormulaTestInputValue(IFormulaTestCase parent, int id) {
        super(parent, id);
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete() {
        ((IFormulaTestCase) getParent()).removeFormulaTestInputValue(this);
        deleted = true;
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type: " + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        identifier = element.getAttribute(PROPERTY_IDENTIFIER);
        value = ValueToXmlHelper.getValueFromElement(element, StringUtils.capitalise(PROPERTY_VALUE));
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_IDENTIFIER, identifier);
        ValueToXmlHelper.addValueToElement(value, element, StringUtils.capitalise(PROPERTY_VALUE));
    }
    
    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("Parameter.gif"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * {@inheritDoc}
     */
    public Parameter findFormulaParameter() throws CoreException {
        if (StringUtils.isEmpty(identifier)){
            return null;
        }
        IFormulaTestCase f = (IFormulaTestCase) getParent();
        IConfigElement cf = (IConfigElement) f.getParent();
        IAttribute attribute = cf.findPcTypeAttribute();
        if (attribute == null){
            return null;
        }
        
        int index = identifier.lastIndexOf("."); //$NON-NLS-1$
        String parameterIdentifier = ""; //$NON-NLS-1$
        if (index == -1){
            parameterIdentifier = identifier;
        } else {
            parameterIdentifier = identifier.substring(0, index);
        }
        
        // find the corresponding parameter
        Parameter[] parameters = attribute.getFormulaParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameterIdentifier.equals(parameters[i].getName())){
                return parameters[i];
            }
        }
        return null;
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
    public void setIdentifier(String identifier) {
        String oldIdentifier = this.identifier;
        this.identifier = identifier;
        valueChanged(oldIdentifier, identifier);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String value) {
        String oldValue = this.value;
        this.value = value;
        valueChanged(oldValue, value);
    }

    /**
     * {@inheritDoc}
     */
    public Datatype findDatatypeOfFormulaParameter() throws CoreException {
        Parameter param = findFormulaParameter();
        if (param == null){
            return null;
        }
        Datatype datatype = getIpsProject().findDatatype(param.getDatatype());
        if (datatype instanceof IPolicyCmptType) {
            // if the datatype specifies a policy cmpt type get the datatype of the attribute
            // identified by the formula test input value identifier
            // e.g. "policy.attribute" id specified in the formula input and policy is specified in the parameter
            // thus policy is the policy cmpt type datatype and attribute is the attribute inside policy
            String parameterName = param.getName();
            if (!identifier.startsWith(parameterName)) {
                throw new CoreException(new IpsStatus(NLS.bind(Messages.FormulaTestInputValue_CoreException_WrongIdentifierForParameter,
                        parameterName, identifier)));
            }
            String attributeName = identifier.substring(parameterName.length() + 1);
            IPolicyCmptType policyCmptType = (IPolicyCmptType) datatype;
            IAttribute attribute = policyCmptType.getAttribute(attributeName);
            if (attribute == null){
                throw new CoreException(new IpsStatus(NLS.bind(
                        Messages.FormulaTestInputValue_CoreException_AttributeOfParameterNotFound, attributeName, identifier)));
            }
            datatype = attribute.findDatatype();
        }
        return datatype;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);

        Parameter param = findFormulaParameter();
        if (param == null) {
            String text = NLS.bind(Messages.FormulaTestInputValue_ValidationMessage_FormulaParameterNotFound, identifier);
            list.add(new Message(MSGCODE_FORMULA_PARAMETER_NOT_FOUND, text, Message.ERROR, this, PROPERTY_IDENTIFIER));
        } else {
            Datatype datatype = findDatatypeOfFormulaParameter();
            if (!(datatype instanceof ValueDatatype)) {
                String text = NLS.bind(Messages.FormulaTestInputValue_ValidationMessage_UnsupportedDatatype,
                        datatype, identifier);
                list.add(new Message(MSGCODE_FORMULA_PARAMETER_HAS_UNSUPPORTED_DATATYPE, text, Message.ERROR, this,
                        PROPERTY_IDENTIFIER));
            } else {
                ValidationUtils.checkValue(datatype.getQualifiedName(), value, this, PROPERTY_VALUE, list);
            }
        }
    }
}
