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
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FormulaTestInputValue extends AtomicIpsObjectPart implements IFormulaTestInputValue {

    /** Tags */
    final static String TAG_NAME = "FormulaTestInputValue"; //$NON-NLS-1$
    
    private String identifier = ""; //$NON-NLS-1$
    private String value = ""; //$NON-NLS-1$
    
    public FormulaTestInputValue(IFormulaTestCase parent, int id) {
        super(parent, id);
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
    public IParameter findFormulaParameter(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(identifier)){
            return null;
        }
        IFormulaTestCase testcase = (IFormulaTestCase) getParent();
        IFormula formula = testcase.getFormula();
        IMethod method = formula.findFormulaSignature(ipsProject);
        if (method== null){
            return null;
        }
        //TODO pk 2007-10-19 this kind of code appears in several areas. Think about refactoring it
        int index = identifier.lastIndexOf("."); //$NON-NLS-1$
        String parameterIdentifier = ""; //$NON-NLS-1$
        if (index == -1){
            parameterIdentifier = identifier;
        } else {
            parameterIdentifier = identifier.substring(0, index);
        }
        
        // find the corresponding parameter
        IParameter[] parameters = method.getParameters();
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
    public ValueDatatype findDatatypeOfFormulaParameter(IIpsProject ipsProject) throws CoreException {
        IParameter param = findFormulaParameter(ipsProject);
        if (param == null){
            return null;
        }
        Datatype datatype = getIpsProject().findDatatype(param.getDatatype());
        if (datatype instanceof IType) {
            // if the datatype specifies an IType get the datatype of the attribute
            // identified by the formula test input value identifier
            // e.g. "policy.attribute" id specified in the formula input and policy is specified in
            // the parameter
            // thus policy is the policy cmpt type datatype and attribute is the attribute inside
            // policy
            String parameterName = param.getName();
            if (!identifier.startsWith(parameterName)) {
                // error because the PolicyCmptType datatype was found with an invalid identifier,
                // this should never happen
                throw new CoreException(new IpsStatus(NLS.bind(
                        Messages.FormulaTestInputValue_CoreException_WrongIdentifierForParameter, parameterName,
                        identifier)));
            }
            String attributeName = identifier.substring(parameterName.length() + 1);
            IType type = (IType) datatype;
            IAttribute attribute = type.findAttribute(attributeName, ipsProject);
            if (attribute == null){
                // attribute not found, therfore the datatype couldn't be determined, 
                // remark this inconsistence will be reported in the vaildate method
                return null;
            }
            return attribute.findDatatype(ipsProject);
        }
        return (ValueDatatype)datatype;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        //TODO pk: seems not to be correct to get the IpsProject like this. Should be provided as parameter
        IIpsProject ipsProject = getIpsProject();
        IParameter param = findFormulaParameter(ipsProject);
        if (param == null) {
            String text = NLS.bind(Messages.FormulaTestInputValue_ValidationMessage_FormulaParameterNotFound,
                    identifier);
            list.add(new Message(MSGCODE_FORMULA_PARAMETER_NOT_FOUND, text, Message.ERROR, this, PROPERTY_IDENTIFIER));
        } else {
            Datatype datatype = findDatatypeOfFormulaParameter(ipsProject);
            if (datatype == null) {
                // check the cause of the missing datatype
                boolean knownReason = false;
                Datatype datatypeOfParam = getIpsProject().findDatatype(param.getDatatype());
                if (datatypeOfParam instanceof IType) {
                    IAttribute attribute = null;
                    String attributeName = identifier.substring(param.getName().length() + 1);
                    IType type = (IType)datatypeOfParam;

                    attribute = type.findAttribute(attributeName, ipsProject);
                    if (attribute == null) {
                        // attribute not found
                        knownReason = true;
                        String text = NLS.bind(Messages.FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_AttributeNotFound, attributeName);
                        list.add(new Message(MSGCODE_RELATED_ATTRIBUTE_NOT_FOUND, text, Message.ERROR, this,
                                PROPERTY_IDENTIFIER));
                    } else if (attribute.findDatatype(ipsProject) == null) {
                        // datatype of attribute not found
                        knownReason = true;
                        String text = NLS.bind(Messages.FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_DatatypeOfParameterNotFound,
                                attribute.getDatatype(), identifier);
                        list.add(new Message(MSGCODE_DATATYPE_OF_RELATED_ATTRIBUTE_NOT_FOUND, text, Message.WARNING,
                                this, PROPERTY_IDENTIFIER));
                    }
                }
                if (!knownReason) {
                    // unknown reason
                    String text = NLS.bind(Messages.FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_DataypeNotFound, param.getDatatype());
                    list.add(new Message(MSGCODE_DATATYPE_NOT_FOUND, text, Message.ERROR, this, PROPERTY_IDENTIFIER));
                }
            } else if (!(datatype instanceof ValueDatatype)) {
                String text = NLS.bind(Messages.FormulaTestInputValue_ValidationMessage_UnsupportedDatatype, datatype,
                        identifier);
                list.add(new Message(MSGCODE_FORMULA_PARAMETER_HAS_UNSUPPORTED_DATATYPE, text, Message.ERROR, this,
                        PROPERTY_IDENTIFIER));
            } else {
                // the datatype was found, check if the value is valid for the datatype
                ValidationUtils.checkValue(datatype.getQualifiedName(), value, this, PROPERTY_VALUE, list);
            }
        }
    }
}
