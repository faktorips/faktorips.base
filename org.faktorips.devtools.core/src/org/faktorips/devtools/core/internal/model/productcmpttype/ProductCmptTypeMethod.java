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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeMethod extends Method implements IProductCmptTypeMethod {

    private boolean formulaSignatureDefinition = true;
    private String formulaName = "";
    
    
    public ProductCmptTypeMethod(IProductCmptType parent, int id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public String getFormulaName() {
        return formulaName;
    }

    /**
     * {@inheritDoc}
     */
    public void setFormulaName(String newName) {
        String oldName = formulaName;
        formulaName = newName;
        valueChanged(oldName, newName);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isFormulaSignatureDefinition() {
        return formulaSignatureDefinition;
    }

    /**
     * {@inheritDoc}
     */
    public void setFormulaSignatureDefinition(boolean newValue) {
        boolean oldValue = formulaSignatureDefinition;
        formulaSignatureDefinition = newValue;
        if (!formulaSignatureDefinition) {
            formulaName = "";
        } else {
            setAbstract(false);
        }
        valueChanged(oldValue, newValue);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultMethodName() {
        if (isFormulaSignatureDefinition()) {
            return "compute" + StringUtils.capitalise(getFormulaName());
        }
        return "";
    }
    
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        formulaSignatureDefinition = Boolean.valueOf(element.getAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION)).booleanValue();
        formulaName = element.getAttribute(PROPERTY_FORMULA_NAME);
    }

    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION, "" + formulaSignatureDefinition);
        element.setAttribute(PROPERTY_FORMULA_NAME, formulaName);
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyName() {
        if (formulaSignatureDefinition) {
            return formulaName;
        }
        return "";
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.FORMULA;
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyDatatype() {
        return getDatatype();
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList result) throws CoreException {
        super.validateThis(result);
        if (!isFormulaSignatureDefinition()) {
            return;
        }
        IIpsProject ipsProject = getIpsProject();
        if (StringUtils.isEmpty(formulaName)) {
            String text = "The formula name is empty!";
            result.add(new Message(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY, text, Message.ERROR, this, IProductCmptTypeMethod.PROPERTY_FORMULA_NAME));
        }
        Datatype datatype = findDatatype(ipsProject);
        if (datatype!=null) {
            if (datatype.isVoid() || !datatype.isValueDatatype()) {
                String text = "Formula signature return type must be a value datatype!";
                result.add(new Message(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES, text, Message.ERROR, this, IMethod.PROPERTY_DATATYPE));
            }
        }
        if (isAbstract()) {
            String text = "Formula signatures mustn't be marked as abstract! The decision if formulas are executed via overriding this method with compiled Java code or by interpreting the formula, is defined by the code generator.";
            result.add(new Message(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT, text, Message.ERROR, this, IMethod.PROPERTY_ABSTRACT));
        }
    }
    
}
