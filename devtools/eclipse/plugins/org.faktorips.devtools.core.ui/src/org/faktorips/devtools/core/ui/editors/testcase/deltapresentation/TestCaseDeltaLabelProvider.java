/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import java.util.ArrayList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.deltapresentation.DeltaCompositeIcon;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseLabelProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestParameter;

/**
 * Label provider for test case type deltas. This label provider makes internal use of the test case
 * label provider to display the tree content of the test case.
 * 
 * @author Joerg Ortmann
 */
final class TestCaseDeltaLabelProvider implements ILabelProvider {

    /** The test case label provider, to display objects in the context of the test case tree */
    private TestCaseLabelProvider testCaseLabelProvider;

    private ArrayList<ILabelProviderListener> listeners = new ArrayList<>();

    private ResourceManager resourceManager;

    public TestCaseDeltaLabelProvider(IIpsProject ipsProject) {
        testCaseLabelProvider = new TestCaseLabelProvider(ipsProject);
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    @Override
    public void dispose() {
        listeners = null;
        resourceManager.dispose();
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof TestCaseDeltaType) {
            return ((TestCaseDeltaType)element).getName();
        } else if (element instanceof TestCaseDeltaWrapperObject) {
            // The element wrapps the test case content tree, therefore
            // delegate the method to the test case content provider
            return ((TestCaseDeltaWrapperObject)element).getText(testCaseLabelProvider);
        } else if (element instanceof ITestAttributeValue) {
            return ((ITestAttributeValue)element).getTestAttribute();
        } else if (element instanceof IIpsObjectPart) {
            return ((IIpsObjectPart)element).getName();
        }
        return Messages.TestCaseDeltaLabelProvider_Undefined;
    }

    @Override
    public Image getImage(Object element) {
        ImageDescriptor descriptor = ImageDescriptor.getMissingImageDescriptor();
        if (element instanceof TestCaseDeltaType) {
            descriptor = ((TestCaseDeltaType)element).getImageDescriptor();
        } else if (element instanceof ITestParameter) {
            descriptor = DeltaCompositeIcon.createAddImage(IpsUIPlugin.getImageHandling().getImage(
                    (ITestParameter)element));
        } else if (element instanceof TestCaseDeltaWrapperObject) {
            // The element wrapps the test case content tree, therefore
            // delegate the method to the test case content provider
            descriptor = ((TestCaseDeltaWrapperObject)element).getImage(testCaseLabelProvider);
        } else if (element instanceof ITestAttributeValue) {
            // if there is a test attribute value then the deleted image will be returned
            descriptor = DeltaCompositeIcon.createDeleteImage(IpsUIPlugin.getImageHandling().getImage(
                    (ITestAttributeValue)element));
        } else if (element instanceof ITestAttribute) {
            // if there is a test attribute then the new image will be returned
            descriptor = DeltaCompositeIcon.createAddImage(IpsUIPlugin.getImageHandling().getImage(
                    (ITestAttribute)element));
        } else if (element instanceof IIpsObjectPart) {
            return IpsUIPlugin.getImageHandling().getImage((IIpsObjectPart)element);
        }
        return (Image)resourceManager.get(descriptor);
    }
}
