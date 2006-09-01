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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterRole;
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
            return IpsPlugin.getDefault().getImage("Datatype.gif"); //$NON-NLS-1$ 
        } else if (element instanceof ITestPolicyCmptTypeParameter){
            return IpsPlugin.getDefault().getImage("PolicyCmptType.gif"); //$NON-NLS-1$ 
        }
        return null; 
	}

	/**
     * Returns the displayed text of the test value parameter or test policy cmpt type param.<br>
     * If the element is a test value parameter then return the name of the param.<br>
     * If the element is a test policy cmpt type param return the name of the param and if a relation is
     * specified and the relation name is not equal the param name return "name (relation)".
     * 
	 * {@inheritDoc}
	 */
	public String getText(Object element) {
        if (element instanceof ITestValueParameter) {
            ITestValueParameter testValueParam = (ITestValueParameter)element;
            return testValueParam.getName() + getRoleExtension(testValueParam.getTestParameterRole());
        } else if (element instanceof ITestPolicyCmptTypeParameter) {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)element;

            String targetExtension = "";
            
            targetExtension = testPolicyCmptTypeParam.getPolicyCmptType()==null?"":StringUtil.unqualifiedName(testPolicyCmptTypeParam.getPolicyCmptType());
            if (StringUtils.isNotEmpty(targetExtension)
                    && !targetExtension.equals(testPolicyCmptTypeParam.getName()))
                targetExtension = " (" + targetExtension + ")";
            else
                // no relation or relation is equal test param name
                targetExtension = "";                

            return testPolicyCmptTypeParam.getName() + targetExtension + getRoleExtension(testPolicyCmptTypeParam.getTestParameterRole());
        } else if (element instanceof ITestAttribute) {
            ITestAttribute testAttribute = (ITestAttribute)element;
            String extension = "";
            if (StringUtils.isNotEmpty(testAttribute.getAttribute())
                    && !testAttribute.getAttribute().equals(testAttribute.getName())) {
                extension = " (" + testAttribute.getAttribute() + ")";
            }
            return StringUtils.capitalise(testAttribute.getName()) + extension + getRoleExtension(testAttribute.getTestAttributeRole());
        }
            
		return "<undefined>";
	}

    /**
     * Returns the role extension of the given role, format: " - roleName"
     */
    private String getRoleExtension(TestParameterRole role){
        return " - " + role.getName();
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
