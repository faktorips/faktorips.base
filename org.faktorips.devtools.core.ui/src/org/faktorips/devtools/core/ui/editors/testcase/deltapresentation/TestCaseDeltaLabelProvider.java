/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.ui.editors.deltapresentation.DeltaCompositeIcon;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseLabelProvider;

/**
 * Label provider for test case type deltas. This label provider makes internal use of the test case
 * label provider to display the tree content of the test case.
 * 
 * @author Joerg Ortmann
 */
final class TestCaseDeltaLabelProvider implements ILabelProvider {
    // The test case label provider, to display objetcs in the context of the test case tree
    private TestCaseLabelProvider testCaseLabelProvider;
    
    private ArrayList listeners = new ArrayList();

    public TestCaseDeltaLabelProvider(IIpsProject ipsProject){
        this.testCaseLabelProvider = new TestCaseLabelProvider(ipsProject);
    }
    
	/**
	 * {@inheritDoc}
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
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
	public void dispose() {
		listeners = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListener(ILabelProviderListener listener) {
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText(Object element) {
        if (element instanceof TestCaseDeltaType){
            return ((TestCaseDeltaType) element).getName();
        } else if (element instanceof TestCaseDeltaWrapperObject){
            // The element wrapps the test case content tree, therefore
            // delegate the method to the test case content provider
            return ((TestCaseDeltaWrapperObject)element).getText(testCaseLabelProvider);
        } else if (element instanceof ITestAttributeValue){
            return ((ITestAttributeValue)element).getTestAttribute();
        } else if (element instanceof IIpsObjectPart){
            return ((IIpsObjectPart)element).getName();
        }
		return Messages.TestCaseDeltaLabelProvider_Undefined;
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage(Object element) {
        if (element instanceof TestCaseDeltaType){
            return ((TestCaseDeltaType) element).getImage();
        } else if (element instanceof ITestParameter){
            return DeltaCompositeIcon.createAddImage(((ITestParameter)element).getImage());
        } else if (element instanceof TestCaseDeltaWrapperObject){
            // The element wrapps the test case content tree, therefore
            // delegate the method to the test case content provider            
            return ((TestCaseDeltaWrapperObject)element).getImage(testCaseLabelProvider);
        } else if (element instanceof ITestAttributeValue){
            // if there is a test attribute value then the deleted image will be returned
            return DeltaCompositeIcon.createDeleteImage(((ITestAttributeValue)element).getImage());
        }  else if (element instanceof ITestAttribute){
            // if there is a test attribute then the new image will be returned
            return DeltaCompositeIcon.createAddImage(((ITestAttribute)element).getImage());
        }  else if (element instanceof IIpsObjectPart){
            return ((IIpsObjectPart)element).getImage();
        }
		return null;
	}
}
