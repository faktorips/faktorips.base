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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.testcase.TestPolicyCmpt;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
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
    private final IObservableValue canShowPolicyComponentType;

    public TestCaseLabelProvider(IIpsProject ipsProject) {
        this(ipsProject, new WritableValue(Boolean.TRUE, Boolean.class));
    }

    public TestCaseLabelProvider(IIpsProject ipsProject, IObservableValue canShowPolicyComponentType) {
        this.ipsProject = ipsProject;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        this.canShowPolicyComponentType = canShowPolicyComponentType;
        this.canShowPolicyComponentType.addValueChangeListener(new IValueChangeListener() {
            @Override
            public void handleValueChange(ValueChangeEvent event) {
                propagateEvent();
            }
        });
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
        if (element instanceof TestPolicyCmpt) {
            TestPolicyCmpt component = (TestPolicyCmpt)element;
            try {
                ITestCase testCase = component.getTestCase();
                IProductCmpt productComponent = component.findProductCmpt(testCase.getIpsProject());
                if (productComponent != null) {
                    Image image = defaultLabelProvider.getImage(productComponent);
                    if (image != null) {
                        return image;
                    }
                }
            } catch (CoreException exception) {
                IpsPlugin.log(exception);
            }
        }
        return (Image)resourceManager.get(getImageDescriptor(element));
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
        if (element instanceof ITestPolicyCmpt) {
            ITestPolicyCmpt tstPolicyCmpt = (ITestPolicyCmpt)element;
            String name = tstPolicyCmpt.getName();
            return name + getLabelExtensionForTestPolicyCmpt(tstPolicyCmpt);
        } else if (element instanceof ITestPolicyCmptLink) {
            ITestPolicyCmptLink testPcTypeLink = (ITestPolicyCmptLink)element;
            return TestCaseHierarchyPath.unqualifiedName(testPcTypeLink.getTestPolicyCmptTypeParameter());
        } else if (element instanceof ITestRule) {
            ITestRule testRule = (ITestRule)element;
            String extForPolicyCmptForValidationRule = getLabelExtensionForTestRule(testRule);
            return testRule.getValidationRule() + extForPolicyCmptForValidationRule;
        } else if (element instanceof ITestObject) {
            return ((ITestObject)element).getTestParameterName();
        } else if (element instanceof TestCaseTypeAssociation) {
            TestCaseTypeAssociation dummyAssociation = (TestCaseTypeAssociation)element;
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
        return StringUtils.EMPTY;
    }

    private String getLabelExtensionForTestPolicyCmpt(ITestPolicyCmpt object) {
        if (hideExtension()) {
            return StringUtils.EMPTY;
        }
        ITestPolicyCmpt tstPolicyCmpt = object;
        String name = tstPolicyCmpt.getName();
        IPolicyCmptType policyCmptType = tstPolicyCmpt.findPolicyCmptType();
        if (policyCmptType == null) {
            return StringUtils.EMPTY;
        }

        String unqualifiedPolicyCmptTypeName = StringUtil.unqualifiedName(policyCmptType.getQualifiedName());
        if (name.equals(unqualifiedPolicyCmptTypeName)) {
            return StringUtils.EMPTY;
        }
        return " : " + unqualifiedPolicyCmptTypeName; //$NON-NLS-1$
    }

    private boolean hideExtension() {
        return canShowPolicyComponentType.getValue() != Boolean.TRUE;
    }

    /**
     * Returns the extension for the test rule: " - <policy cmpt type name>"
     */
    private String getLabelExtensionForTestRule(ITestRule testRule) {
        String extForPolicyCmptForValidationRule = ""; //$NON-NLS-1$
        IValidationRule validationRule;
        try {
            validationRule = testRule.findValidationRule(ipsProject);
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
        return target.replaceAll(TestCaseHierarchyPath.SEPARATOR, "/"); //$NON-NLS-1$
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
