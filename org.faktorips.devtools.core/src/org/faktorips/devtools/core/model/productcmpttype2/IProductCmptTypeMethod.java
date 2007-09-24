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

package org.faktorips.devtools.core.model.productcmpttype2;

import org.faktorips.devtools.core.model.ProgramingLanguage;
import org.faktorips.devtools.core.model.type.IMethod;

/**
 * Method signatures for product component types extend the "normal" method. The provide an implementation type
 * that defines how the method is implemented. 
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeMethod extends IMethod, IProdDefProperty {

    public final static String PROPERTY_FORMULA_SIGNATURE_DEFINITION = "formulaSignatureDefinition";
    public final static String PROPERTY_FORMULA_NAME = "formulaName";
    
    public final static String PROPERTY_IMPLEMENTATION_TYPE= "implementationType";
    
    /**
     * Returns the implementation type. This method never returns null.
     */
    public ImplementationType getImplementationType();
    
    /**
     * Returns the language in that this method is implemented. 
     */
    public ProgramingLanguage getImplementedIn();
    
    /**
     * Returns <code>true</code> if this is formula signature definition, <code>false</code> if
     * it is not. 
     */
    public boolean isFormulaSignatureDefinition();

    public void setFormulaSignatureDefinition(boolean newValue);
    
    public String getFormulaName();
    
    public void setFormulaName(String newName);
    
    public String getDefaultMethodName();
}
