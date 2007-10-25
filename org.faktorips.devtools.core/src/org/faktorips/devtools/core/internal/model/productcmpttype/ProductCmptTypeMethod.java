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
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.model.ProgramingLanguage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ImplementationType;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
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
    public ImplementationType getImplementationType() {
        return formulaSignatureDefinition ? ImplementationType.IN_PRODUCT_CMPT_GENERATION : ImplementationType.IN_TYPE;
    }
    
    /**
     * {@inheritDoc}
     */
    public ProgramingLanguage getImplementedIn() {
        return formulaSignatureDefinition ? ProgramingLanguage.FAKTORIPS_FORMULA : ProgramingLanguage.JAVA;
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
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }
    
    
}
