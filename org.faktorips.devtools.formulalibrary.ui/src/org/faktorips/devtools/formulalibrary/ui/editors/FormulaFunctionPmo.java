package org.faktorips.devtools.formulalibrary.ui.editors;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.util.message.ObjectProperty;

/**
 * The <tt>FormulaFunctionPmo</tt> is a presentation object model for <tt>IFormulaFunction</tt>.
 * 
 * @author HBaagil
 */
public class FormulaFunctionPmo extends IpsObjectPartPmo {

    public static final String PROPERTY_FORMULA_NAME = "formulaName"; //$NON-NLS-1$
    public static final String PROPERTY_RETURN_TYPE = "returnType"; //$NON-NLS-1$
    public static final String PROPERTY_FORMULA_EXPRESSION = "formulaExpression"; //$NON-NLS-1$

    /**
     * Creates a new <tt>FormulaFunctionPmo</tt>.
     */
    public FormulaFunctionPmo() {
        super();
    }

    /**
     * Sets new <tt>IIpsModel</tt> as <tt>IFormulaFunction</tt>.
     * 
     * @param formulaFunction The <tt>IFormulaFunctoin</tt> to be pass.
     */
    public void setFormulaFunction(IFormulaFunction formulaFunction) {
        updateObjectPropertyMappings(formulaFunction);
        setIpsObjectPartContainer(formulaFunction);
    }

    private void updateObjectPropertyMappings(IFormulaFunction formulaFunction) {
        clearObjectPropertyMappings();
        if (formulaFunction != null) {
            setUpObjectPropertyMapping(formulaFunction);
        }
    }

    private void setUpObjectPropertyMapping(IFormulaFunction formulaFunction) {
        ObjectProperty modelFormulaName = new ObjectProperty(formulaFunction.getFormulaMethod(),
                IFormulaMethod.PROPERTY_FORMULA_NAME);
        ObjectProperty uiFormulaName = new ObjectProperty(this, PROPERTY_FORMULA_NAME);
        ObjectProperty modelIpsElementName = new ObjectProperty(formulaFunction.getFormulaMethod(),
                IIpsElement.PROPERTY_NAME);
        ObjectProperty modelFormulaDatatype = new ObjectProperty(formulaFunction.getFormulaMethod(),
                IBaseMethod.PROPERTY_DATATYPE);
        ObjectProperty uiReturnType = new ObjectProperty(this, PROPERTY_RETURN_TYPE);
        // do not map the property for the expression because we do not want a error marker at the
        // expression text

        mapValidationMessagesFor(modelFormulaName).to(uiFormulaName);
        mapValidationMessagesFor(modelIpsElementName).to(uiFormulaName);
        mapValidationMessagesFor(modelFormulaDatatype).to(uiReturnType);
    }

    /**
     * Returns <tt>IFormulaFunction</tt> of this.
     * 
     * @return IFormulaFunction
     */
    public IFormulaFunction getFormulaFunction() {
        return (IFormulaFunction)getIpsObjectPartContainer();
    }

    /**
     * Returns formula name of this.
     * 
     * @return String
     */
    public String getFormulaName() {
        if (getFormulaFunction() != null) {
            return getFormulaFunction().getFormulaMethod().getFormulaName();
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Sets formula, method and fromula signature name of this.
     * 
     * @param formulaName The name to be set to this.
     */
    public void setFormulaName(String formulaName) {
        if (getFormulaFunction() != null) {
            getFormulaFunction().getFormulaMethod().setFormulaName(formulaName);
            getFormulaFunction().getFormulaMethod().setName(formulaName);
            getFormulaFunction().getExpression().setFormulaSignature(formulaName);
        }
    }

    /**
     * Returns return type of this.
     * 
     * @return String
     */

    public String getReturnType() {
        if (getFormulaFunction() != null) {
            return getFormulaFunction().getFormulaMethod().getDatatype();
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Sets return type for this.
     * 
     * @param newReturnType The <tt>String</tt> name for return type.
     */

    public void setReturnType(String newReturnType) {
        if (getFormulaFunction() != null) {
            getFormulaFunction().getFormulaMethod().setDatatype(newReturnType);
        }
    }

    /**
     * Returns expression of this.
     * 
     * @return String
     */
    public String getFormulaExpression() {
        if (getFormulaFunction() != null) {
            return getFormulaFunction().getExpression().getExpression();
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Sets expression for this.
     * 
     */
    public void setFormulaExpression(String expression) {
        if (getFormulaFunction() != null) {
            getFormulaFunction().getExpression().setExpression(expression);
        }
    }
}
