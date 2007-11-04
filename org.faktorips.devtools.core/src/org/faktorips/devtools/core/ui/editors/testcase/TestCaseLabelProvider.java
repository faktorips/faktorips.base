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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Label provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseLabelProvider implements ILabelProvider {
	
	/**
	 * {@inheritDoc}
	 */
	public Image getImage(Object element) {
        if (element instanceof TestCaseTypeRelation){
	    	return getImageFromRelationType((TestCaseTypeRelation)element);
	    } else if (element instanceof IIpsObjectPart) {
            return ((IIpsObjectPart) element).getImage();
        } else if (element instanceof TestCaseTypeRule) {
            return ((TestCaseTypeRule) element).getImage();
        }
        return null; 
	}

	/**
     * Returns the image of the given relation test case type parameter.
     */
    private Image getImageFromRelationType(TestCaseTypeRelation dummyRelation) {
        ITestPolicyCmptTypeParameter typeParam = dummyRelation.getTestPolicyCmptTypeParam();
        if (typeParam != null)
            return typeParam.getImage();

        return null;
    }

	/**
	 * {@inheritDoc}
	 */
	public String getText(Object element) {
		if (element instanceof ITestPolicyCmpt) {
			ITestPolicyCmpt tstPolicyCmpt = (ITestPolicyCmpt) element;
			return tstPolicyCmpt.getName();
		} else if (element instanceof ITestPolicyCmptRelation) {
			ITestPolicyCmptRelation testPcTypeRelation = (ITestPolicyCmptRelation) element;
			String text = ""; //$NON-NLS-1$
			try {
				text = TestCaseHierarchyPath.unqualifiedName(testPcTypeRelation.getTestPolicyCmptTypeParameter());
				
				ITestPolicyCmptTypeParameter typeParam = testPcTypeRelation.findTestPolicyCmptTypeParameter();
				if (typeParam != null && typeParam.isRequiresProductCmpt())
					text += Messages.TestCaseLabelProvider_LabelSuffix_RequiresProductCmpt;
			} catch (CoreException e) {
				// ignore model error, the model consitence between the test case type and the test case
				// will be check when openening the editor, therefor we can ignore is here
			}
			return text;
		} else if (element instanceof ITestRule){
            ITestRule testRule = (ITestRule)element;
            String extForPolicyCmptForValidationRule = getLabelExtensionForTestRule(testRule);
            return testRule.getValidationRule() + extForPolicyCmptForValidationRule;
        } else if (element instanceof ITestObject){
			return ((ITestObject)element).getTestParameterName();
	    } else if(element instanceof TestCaseTypeRelation){
	    	TestCaseTypeRelation dummyRelation = (TestCaseTypeRelation) element;
	    	String text = dummyRelation.getName();
	    	if (dummyRelation.isRequiresProductCmpt()){
	    		text += Messages.TestCaseLabelProvider_LabelSuffix_RequiresProductCmpt;
	    	}
	    	return text;
	    } else if (element instanceof IIpsObjectPart){
	        // e.g. tree node element for test rule parameters
            return ((IIpsObjectPart) element).getName();
        } else if (element instanceof TestCaseTypeRule) {
            return ((TestCaseTypeRule) element).getName();
        }
		return Messages.TestCaseLabelProvider_undefined;
	}

    /*
     * Returns the extension for the test rule: " - <policy cmpt type name>"
     */
    private String getLabelExtensionForTestRule(ITestRule testRule) {
        String extForPolicyCmptForValidationRule = ""; //$NON-NLS-1$
        IValidationRule validationRule;
        try {
            validationRule = testRule.findValidationRule();
            if (validationRule != null) {
                extForPolicyCmptForValidationRule = " - " + ((PolicyCmptType)validationRule.getParent()).getName(); //$NON-NLS-1$
            }
        } catch (CoreException e) {
            // ignore exception, return empty extension 
        }
        return extForPolicyCmptForValidationRule;
    }
    
    /**
     * Returns the title text of a section which displays the given test policy cmpt.<br>
     * Returns the name of the test policy cmpt and if the name is not equal to the test policy cmpt
     * type param name the name of the parm after " : "<br>
     * Return format: name : test policy cmpt type param name
     */
    public String getTextForSection(ITestPolicyCmpt testPolicyCmpt){
        String sectionText = testPolicyCmpt.getName();
        if (! testPolicyCmpt.getName().equals(testPolicyCmpt.getTestPolicyCmptTypeParameter()))
            sectionText += " : " + testPolicyCmpt.getTestPolicyCmptTypeParameter(); //$NON-NLS-1$
        return sectionText;
    }

    /**
     * Returns the title text of a section which displays the given test policy cmpt relation (e.g. assoziation).<br>
     * Returns the name of the test policy cmpt type param.
     */
    public String getTextForSection(ITestPolicyCmptRelation currRelation){
        return currRelation.getTestPolicyCmptTypeParameter();
    }
    
    /**
     * Returns the title text of a section which displays the given test value.<br>
     * Returns the name of the test value.
     */
    public String getTextForSection(ITestValue testValue){
        return StringUtils.capitalise(testValue.getTestValueParameter());
    }

    /**
     * Returns the title text of a section which displays the given test rule.<br>
     * Returns the validation rule and the corresponding policy cmpt.
     */
    public String getTextForSection(ITestRule testRule){
        return getText(testRule);
    }
    
    /**
     * Returns the label for the target of a assoziation.
     */
    public String getAssoziationTargetLabel(String target) {
        return target.replaceAll(TestCaseHierarchyPath.SEPARATOR, "/"); //$NON-NLS-1$
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
		return true;
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
