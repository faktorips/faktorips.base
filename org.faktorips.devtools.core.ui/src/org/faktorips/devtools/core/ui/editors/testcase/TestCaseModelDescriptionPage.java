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

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.ui.views.modeldescription.DefaultModelDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.DescriptionItem;

/**
 * A page for presenting the properties of a {@link ITestCase}. This page is
 * connected to a {@link TestCaseEditor} similiar to the outline view.
 *
 * @author Joerg Ortmann
 */
public class TestCaseModelDescriptionPage extends DefaultModelDescriptionPage implements ITestCaseDetailAreaRedrawListener {

    private TestCaseEditor editor;
    private ITestCase testCase;

    public TestCaseModelDescriptionPage(TestCaseEditor editor) throws CoreException {
    	super();
        this.editor = editor;
        this.testCase = editor.getTestCase();
        super.setTitle(testCase.getName());
        
        this.editor.addDetailAreaRedrawListener(this);
        
        updateDescriptionItems(Arrays.asList(testCase.getTestObjects()));
    }

    private void updateDescriptionItems(List<ITestObject> visibleTestObjects) throws CoreException {
        ArrayList<DescriptionItem> desrList = new ArrayList<DescriptionItem>(visibleTestObjects.size());
        Set<IIpsObjectPart> uniqueTestObjects = new HashSet<IIpsObjectPart>(visibleTestObjects.size());
        for (Iterator<ITestObject> iter = visibleTestObjects.iterator(); iter.hasNext();) {
            ITestObject testObject = iter.next();
            ITestParameter parameter = testObject.findTestParameter(testCase.getIpsProject());
            if (parameter == null){
                continue;
            }
            if (testObject instanceof ITestPolicyCmpt){
                // description of test policy cmpt are currently not supported
                // only test attributes
                addChildTestObjetcs(testCase.getIpsProject(), (ITestPolicyCmpt)testObject, desrList, uniqueTestObjects);
            } else {
                addUniqueDescriptionItem(parameter, desrList, uniqueTestObjects, parameter.getName());
            }
        }
        
        super.setDescriptionItems(desrList.toArray(new DescriptionItem[desrList.size()]));
    }

    private void addUniqueDescriptionItem(IIpsObjectPart ipsObjectPart,
            ArrayList<DescriptionItem> desrList,
            Set<IIpsObjectPart> uniqueTestObjects,
            String parameterName) {
        String name = ipsObjectPart.getName();
        if (uniqueTestObjects.contains(ipsObjectPart)) {
            return;
        } else {
            uniqueTestObjects.add(ipsObjectPart);
        }
        String desrcItemName = name.equals(parameterName) ? name : parameterName + " : " + name; //$NON-NLS-1$
        desrList.add(new DescriptionItem(desrcItemName, ipsObjectPart.getDescription()));
    }

    private void addChildTestObjetcs(IIpsProject ipsProject,
            ITestPolicyCmpt cmpt,
            ArrayList<DescriptionItem> desrList,
            Set<IIpsObjectPart> uniqueTestObjects) throws CoreException {
        ITestParameter parameter = cmpt.findTestParameter(testCase.getIpsProject());
        String parameterName = ""; //$NON-NLS-1$
        if (parameter != null) {
            parameterName = parameter.getName();
        }
        // add description for attributes
        ITestAttributeValue[] testAttributeValues = cmpt.getTestAttributeValues();
        for (int i = 0; i < testAttributeValues.length; i++) {
            ITestAttributeValue value = testAttributeValues[i];
            ITestAttribute attribute = value.findTestAttribute(ipsProject);
            if (attribute == null) {
                continue;
            }
            addUniqueDescriptionItem(attribute, desrList, uniqueTestObjects, parameterName);
        }
        ITestPolicyCmptLink[] testPolicyCmptLinks = cmpt.getTestPolicyCmptLinks();
        for (int i = 0; i < testPolicyCmptLinks.length; i++) {
            if (testPolicyCmptLinks[i].isComposition()) {
                ITestPolicyCmpt target = testPolicyCmptLinks[i].findTarget();
                if (target == null) {
                    continue;
                }
                addChildTestObjetcs(ipsProject, target, desrList, uniqueTestObjects);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visibleTestObjectsChanges(List<ITestObject> visibleTestObjects) throws CoreException {
        updateDescriptionItems(visibleTestObjects);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        super.dispose();
        editor.removeDetailAreaRedrawListener(this);
    }
}
