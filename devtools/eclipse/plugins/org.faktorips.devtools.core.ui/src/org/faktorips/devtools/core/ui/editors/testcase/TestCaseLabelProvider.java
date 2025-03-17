/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.internal.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.model.internal.testcase.TestPolicyCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.StringUtil;

/**
 * Label provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseLabelProvider extends StyledCellLabelProvider implements ILabelProvider {

    private IIpsProject ipsProject;

    private ResourceManager resourceManager;

    /** Provides default images for the elements. */
    private DefaultLabelProvider defaultLabelProvider = new DefaultLabelProvider();

    /**
     * Determines whether the extension (i.e. the actual policy class name) is shown for product
     * components.
     */
    private final IObservableValue<Boolean> canShowPolicyComponentType;

    public TestCaseLabelProvider(IIpsProject ipsProject) {
        this(ipsProject, new WritableValue<>(Boolean.TRUE, Boolean.class));
    }

    public TestCaseLabelProvider(IIpsProject ipsProject, IObservableValue<Boolean> canShowPolicyComponentType) {
        this.ipsProject = ipsProject;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        this.canShowPolicyComponentType = canShowPolicyComponentType;
        this.canShowPolicyComponentType.addValueChangeListener($ -> propagateEvent());
    }

    /**
     * Propagates the {@link ValueChangeEvent} of the property {@link #canShowPolicyComponentType}
     * to all registered listeners of this label provider.
     */
    protected void propagateEvent() {
        fireLabelProviderChanged(new LabelProviderChangedEvent(this));
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof TestPolicyCmpt component) {
            try {
                ITestCase testCase = component.getTestCase();
                IProductCmpt productComponent = component.findProductCmpt(testCase.getIpsProject());
                if (productComponent != null) {
                    Image image = defaultLabelProvider.getImage(productComponent);
                    if (image != null) {
                        return image;
                    }
                }
            } catch (IpsException exception) {
                IpsPlugin.log(exception);
            }
        }
        return resourceManager.get(getImageDescriptor(element));
    }

    public ImageDescriptor getImageDescriptor(Object element) {
        if (element instanceof TestCaseTypeAssociation) {
            return getImageFromAssociationType((TestCaseTypeAssociation)element);
        } else if (element instanceof IIpsObjectPart) {
            return IpsUIPlugin.getImageHandling().getImageDescriptor((IIpsObjectPart)element);
        } else if (element instanceof TestCaseTypeRule) {
            return ((TestCaseTypeRule)element).getImageDescriptor();
        }
        return ImageDescriptor.getMissingImageDescriptor();
    }

    /**
     * Returns the image of the given association test case type parameter.
     */
    private ImageDescriptor getImageFromAssociationType(TestCaseTypeAssociation dummyAssociation) {
        ITestPolicyCmptTypeParameter typeParam = dummyAssociation.getTestPolicyCmptTypeParam();
        if (typeParam == null) {
            return null;
        }
        if (dummyAssociation.getParentTestPolicyCmpt() == null) {
            // root node
            return getImageForRootPolicyCmptTypeParamNode();
        } else {
            return IpsUIPlugin.getImageHandling().getImageDescriptor(typeParam);
        }
    }

    public ImageDescriptor getImageForRootPolicyCmptTypeParamNode() {
        return IpsUIPlugin.getImageHandling().createImageDescriptor("TestParameterRootObject.gif"); //$NON-NLS-1$
    }

    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        updateCell(cell, element);
        super.update(cell);
    }

    private void updateCell(ViewerCell cell, Object item) {
        String suffix = getSuffixFor(item);
        StyleRange styledPath = new StyleRange();
        String name = getName(item);
        styledPath.start = name.length();
        styledPath.length = suffix.length();
        styledPath.foreground = getCurrentDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        styledPath.fontStyle = SWT.NORMAL;
        cell.setText(name + suffix);
        cell.setStyleRanges(new StyleRange[] { styledPath });
        cell.setImage(getImage(item));
    }

    /**
     * Returns the name of the object.
     */
    private String getName(Object object) {
        if (object instanceof ITestPolicyCmpt) {
            return ((ITestPolicyCmpt)object).getName();
        } else if (object instanceof ITestRule) {
            return ((ITestRule)object).getValidationRule();
        }
        // the default for unspecified objects is the label,
        // because these objects didn't have a suffix, thus the label
        // is always equal to the name
        return getText(object);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ITestPolicyCmpt tstPolicyCmpt) {
            String name = tstPolicyCmpt.getName();
            return name + getLabelExtensionForTestPolicyCmpt(tstPolicyCmpt);
        } else if (element instanceof ITestPolicyCmptLink testPcTypeLink) {
            return TestCaseHierarchyPath.unqualifiedName(testPcTypeLink.getTestPolicyCmptTypeParameter());
        } else if (element instanceof ITestRule testRule) {
            String extForPolicyCmptForValidationRule = getLabelExtensionForTestRule(testRule);
            return testRule.getValidationRule() + extForPolicyCmptForValidationRule;
        } else if (element instanceof ITestObject) {
            return ((ITestObject)element).getTestParameterName();
        } else if (element instanceof TestCaseTypeAssociation dummyAssociation) {
            return dummyAssociation.getName();
        } else if (element instanceof IIpsObjectPart) {
            // e.g. tree node element for test rule parameters
            return ((IIpsObjectPart)element).getName();
        } else if (element instanceof TestCaseTypeRule) {
            return ((TestCaseTypeRule)element).getName();
        }
        return Messages.TestCaseLabelProvider_undefined;
    }

    private String getSuffixFor(Object object) {
        if (object instanceof ITestPolicyCmpt) {
            return getLabelExtensionForTestPolicyCmpt((ITestPolicyCmpt)object);
        } else if (object instanceof ITestRule) {
            return getLabelExtensionForTestRule((ITestRule)object);
        }
        return IpsStringUtils.EMPTY;
    }

    private String getLabelExtensionForTestPolicyCmpt(ITestPolicyCmpt object) {
        if (hideExtension()) {
            return IpsStringUtils.EMPTY;
        }
        ITestPolicyCmpt tstPolicyCmpt = object;
        String name = tstPolicyCmpt.getName();
        IPolicyCmptType policyCmptType = tstPolicyCmpt.findPolicyCmptType();
        if (policyCmptType == null) {
            return IpsStringUtils.EMPTY;
        }

        String unqualifiedPolicyCmptTypeName = StringUtil.unqualifiedName(policyCmptType.getQualifiedName());
        if (name.equals(unqualifiedPolicyCmptTypeName)) {
            return IpsStringUtils.EMPTY;
        }
        return " : " + unqualifiedPolicyCmptTypeName; //$NON-NLS-1$
    }

    private boolean hideExtension() {
        return canShowPolicyComponentType.getValue() != Boolean.TRUE;
    }

    /**
     * Returns the extension for the test rule: " - &lt;policy cmpt type name&gt;"
     */
    private String getLabelExtensionForTestRule(ITestRule testRule) {
        String extForPolicyCmptForValidationRule = ""; //$NON-NLS-1$
        IValidationRule validationRule;
        try {
            validationRule = testRule.findValidationRule(ipsProject);
            if (validationRule != null) {
                extForPolicyCmptForValidationRule = " - " + ((IPolicyCmptType)validationRule.getParent()).getName(); //$NON-NLS-1$
            }
        } catch (IpsException e) {
            // ignore exception, return empty extension
        }
        return extForPolicyCmptForValidationRule;
    }

    /**
     * Returns the title text of a section which displays the given test policy cmpt.<br>
     * Returns the name of the test policy cmpt and if the name is not equal to the test policy cmpt
     * name then additionally the name of the test policy cmpt after " : "<br>
     * Return format: test policy cmpt name : policy cmpt type name
     */
    public String getTextForSection(ITestPolicyCmpt testPolicyCmpt) {
        String sectionText = testPolicyCmpt.getName();

        IPolicyCmptType policyCmptType = testPolicyCmpt.findPolicyCmptType();
        if (policyCmptType == null) {
            return sectionText;
        }

        String unqualifiedPolicyCmptTypeName = StringUtil.unqualifiedName(policyCmptType.getQualifiedName());
        if (!sectionText.equals(unqualifiedPolicyCmptTypeName)) {
            return sectionText + " : " + unqualifiedPolicyCmptTypeName; //$NON-NLS-1$
        }

        return sectionText;
    }

    /**
     * Returns the title text of a section which displays the given test policy cmpt link (e.g.
     * assoziation).<br>
     * Returns the name of the test policy cmpt type param.
     */
    public String getTextForSection(ITestPolicyCmptLink currLink) {
        return currLink.getTestPolicyCmptTypeParameter();
    }

    /**
     * Returns the title text of a section which displays the given test value.<br>
     * Returns the name of the test value.
     */
    public String getTextForSection(ITestValue testValue) {
        return StringUtils.capitalize(testValue.getTestValueParameter());
    }

    /**
     * Returns the title text of a section which displays the given test rule.<br>
     * Returns the validation rule and the corresponding policy cmpt.
     */
    public String getTextForSection(ITestRule testRule) {
        return getText(testRule);
    }

    /**
     * Returns the label for the target of a association.
     */
    public String getAssoziationTargetLabel(String target) {
        return target.replace(TestCaseHierarchyPath.SEPARATOR, "/"); //$NON-NLS-1$
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
        defaultLabelProvider.dispose();
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    private Display getCurrentDisplay() {
        return Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
    }
}
