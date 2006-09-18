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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.util.StringUtil;

/**
 * Label provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeLabelProvider implements ILabelProvider {  
    /**
	 * {@inheritDoc}
	 */
	public Image getImage(Object element) {
        if (element instanceof ITestValueParameter){
            return ((ITestValueParameter)element).getImage();
        } else if (element instanceof ITestPolicyCmptTypeParameter){
            return ((ITestPolicyCmptTypeParameter) element).getImage();
        } else if (element instanceof TestCaseTypeTreeRootElement){
            return ((TestCaseTypeTreeRootElement) element).getImgage();
        }
        return null; 
	}

	/**
     * Returns the displayed text of the test value parameter or test policy cmpt type param.<br>
     * If the element is a test value parameter then return the name of the param.<br>
     * If the element is a test policy cmpt type param return the name of the param and if a relation is
     * specified and the target name is not equal the param name return "name : relation".
     * 
	 * {@inheritDoc}
	 */
	public String getText(Object element) {
        if (element instanceof ITestValueParameter) {
            ITestValueParameter testValueParam = (ITestValueParameter)element;
            return testValueParam.getName() + getTypeExtension(testValueParam.getTestParameterType());
        } else if (element instanceof ITestPolicyCmptTypeParameter) {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)element;

            String targetExtension = ""; //$NON-NLS-1$
            
            targetExtension = testPolicyCmptTypeParam.getPolicyCmptType()==null?"":StringUtil.unqualifiedName(testPolicyCmptTypeParam.getPolicyCmptType()); //$NON-NLS-1$
            if (StringUtils.isNotEmpty(targetExtension)
                    && !targetExtension.equals(testPolicyCmptTypeParam.getName()))
                targetExtension = " : " + targetExtension; //$NON-NLS-1$
            else
                // no relation or relation is equal test param name
                targetExtension = "";                 //$NON-NLS-1$

            String productExt = testPolicyCmptTypeParam.isRequiresProductCmpt()?" (P)":""; //$NON-NLS-1$ //$NON-NLS-2$
            
            return testPolicyCmptTypeParam.getName() + targetExtension + getTypeExtension(testPolicyCmptTypeParam.getTestParameterType()) + productExt;
        } else if (element instanceof ITestAttribute) {
            ITestAttribute testAttribute = (ITestAttribute)element;
            String extension = ""; //$NON-NLS-1$
            if (StringUtils.isNotEmpty(testAttribute.getAttribute())
                    && !testAttribute.getAttribute().equals(testAttribute.getName())) {
                extension = " : " + testAttribute.getAttribute(); //$NON-NLS-1$
            }
            return StringUtils.capitalise(testAttribute.getName()) + extension + getTypeExtension(testAttribute.getTestAttributeType());
        } else if (element instanceof TestCaseTypeTreeRootElement){
            return ((TestCaseTypeTreeRootElement) element).getText();
        }
            
		return Messages.TestCaseTypeLabelProvider_Undefined;
	}

    /**
     * Returns the type extension of the given type, format: " - typeName"
     */
    private String getTypeExtension(TestParameterType type){
        return " - " + type.getName(); //$NON-NLS-1$
    }
    
	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}
    
    /**
     * {@inheritDoc}
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    } 

    /**
     * {@inheritDoc}
     */
    public void removeListener(ILabelProviderListener listener) {
    }

    /**
     * {@inheritDoc}
     */
    public void addListener(ILabelProviderListener listener) {
    }
}
