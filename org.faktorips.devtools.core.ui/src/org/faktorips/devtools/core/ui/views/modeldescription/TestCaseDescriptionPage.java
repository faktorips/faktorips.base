/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * A page for presenting the properties of a {@link ITestCaseType} or {@link ITestCase}. This page
 * is connected to a Editor similar to the outline view.
 * 
 * @author Quirin Stoll
 */
public class TestCaseDescriptionPage extends DefaultModelDescriptionPage {

    public TestCaseDescriptionPage(IpsObjectEditor editor) {
        super();
        IIpsObject ipsObject = editor.getIpsObject();
        if (ipsObject instanceof ITestCaseType) {
            setIpsObject(ipsObject);
        } else if (ipsObject instanceof ITestCase) {
            try {
                setIpsObject(((TestCase)ipsObject).findTestCaseType(ipsObject.getIpsProject()));
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
        setDescriptionData();
    }

    @Override
    protected List<DescriptionItem> createDescriptions() throws CoreException {
        List<DescriptionItem> descriptions = new ArrayList<DescriptionItem>();
        ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParameters = getIpsObject().getTestPolicyCmptTypeParameters();
        for (ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter : testPolicyCmptTypeParameters) {
            ITestAttribute[] testAttributes = testPolicyCmptTypeParameter.getTestAttributes();
            String parameterName = testPolicyCmptTypeParameter.getName();
            for (ITestAttribute testAttribute : testAttributes) {
                String desrcItemName = parameterName + " : " + testAttribute.getAttribute(); //$NON-NLS-1$
                String localizedDescription = IpsPlugin.getMultiLanguageSupport()
                        .getLocalizedDescription(testAttribute);
                descriptions.add(new DescriptionItem(desrcItemName, localizedDescription));
            }
        }
        return descriptions;
    }

    @Override
    public TestCaseType getIpsObject() {
        return (TestCaseType)super.getIpsObject();
    }
}
