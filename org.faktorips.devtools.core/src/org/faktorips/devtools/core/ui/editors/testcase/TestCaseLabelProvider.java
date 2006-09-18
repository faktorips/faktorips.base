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
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
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
        if ((element instanceof ITestPolicyCmptRelation)) {
        	return ((ITestPolicyCmptRelation)element).getImage();
        } else if (element instanceof ITestPolicyCmpt) {
        	return ((ITestPolicyCmpt) element).getImage();
        } else if(element instanceof ITestValue){
        	return ((ITestValue) element).getImage();
	    } else if (element instanceof TestCaseTypeRelation){
	    	return getImageFromRelationType((TestCaseTypeRelation)element);
	    } else if (element instanceof IIpsObjectPart) {
            return ((IIpsObjectPart) element).getImage();
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
		} else if(element instanceof ITestValue){
			ITestValue testValueParameter = (ITestValue) element;
			return testValueParameter.getTestValueParameter(); //$NON-NLS-1$ 
	    } else if(element instanceof TestCaseTypeRelation){
	    	TestCaseTypeRelation dummyRelation = (TestCaseTypeRelation) element;
	    	String text = dummyRelation.getName();
	    	if (dummyRelation.isRequiresProductCmpt()){
	    		text += Messages.TestCaseLabelProvider_LabelSuffix_RequiresProductCmpt;
	    	}
	    	return text;
	    }
		return Messages.TestCaseLabelProvider_undefined;
	}
    
    /**
     * Returns the title text of a section which displays the given test policy cmpt.<br>
     * Returns the name of the test policy cmpt and if the name is not equal to the test policy cmpt type param name 
     * the name of the parm after " : "<br>
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
     * Returns the label for the target of a assoziation.
     */
    public String getAssoziationTargetLabel(String target) {
        return target.replaceAll(TestCaseHierarchyPath.separator, "/"); //$NON-NLS-1$
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
