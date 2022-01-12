/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredList;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypeHierarchy;
import org.faktorips.devtools.model.util.QNameUtil;

/**
 * Wizard page to select one or more policy cmpt type attributes.
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeSelectionWizardPage extends WizardPage {

    private static final String PAGE_ID = "TestAttributeSelectionWizardPage"; //$NON-NLS-1$

    protected FilteredList fFilteredList;

    private NewTestAttributeWizard wizard;

    private Text fFilterText = null;
    private String fFilter = null;

    private boolean fIsMultipleSelection = true;
    private boolean fMatchEmptyString = true;
    private boolean fAllowDuplicates = true;
    private boolean fIgnoreCase = true;
    private boolean showSubtypes;

    private ILabelProvider fRenderer;

    private int fWidth = 60;
    private int fHeight = 18;

    private Checkbox checkbox;

    private ITypeHierarchy typeHierarchy;
    private IPolicyCmptType policyCmptType;
    private ITypeHierarchy subtypeHierarchy;

    protected TestAttributeSelectionWizardPage(NewTestAttributeWizard wizard, boolean showSubtypes) {
        super(PAGE_ID, Messages.TestAttributeSelectionWizardPage_wizardPageTitle, null);

        this.wizard = wizard;
        this.showSubtypes = showSubtypes;

        ITestPolicyCmptTypeParameter parameter = wizard.geTestPolicyCmptTypeParameter();
        String description = NLS.bind(Messages.TestAttributeSelectionWizardPage_wizardPageDescription,
                QNameUtil.getUnqualifiedName(parameter.getPolicyCmptType()));
        setDescription(description);
        policyCmptType = parameter.findPolicyCmptType(wizard.getIpsProjekt());
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit uiToolkit = wizard.getUiToolkit();

        Composite group = uiToolkit.createGridComposite(parent, 1, true, true);

        fFilterText = createFilterText(group);
        fFilteredList = createFilteredList(group);

        checkbox = uiToolkit.createCheckbox(group,
                Messages.AttributeElementListSelectionDialog_ShowAttributesOfSubclasses);
        checkbox.setBackground(group.getBackground());
        checkbox.getButton().setBackground(group.getBackground());
        checkbox.setChecked(showSubtypes);

        CheckboxField field = new CheckboxField(checkbox);
        field.addChangeListener(e -> {
            Runnable runnable = () -> {
                try {
                    showSubtypes = ((CheckboxField)e.field).getCheckbox().isChecked();
                    setListElements(getElements());
                    wizard.setShowSubtypeAttributes(showSubtypes);
                } catch (CoreException ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            };
            BusyIndicator.showWhile(TestAttributeSelectionWizardPage.this.getShell().getDisplay(), runnable);
        });

        setControl(group);

        try {
            init(policyCmptType);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private IPolicyCmptTypeAttribute[] getElements() throws CoreRuntimeException {
        List<? extends IAttribute> attributes = typeHierarchy.getAllAttributesRespectingOverride(policyCmptType);
        List<IPolicyCmptTypeAttribute> attributesInDialog = new ArrayList<>();
        for (IAttribute attribute : attributes) {
            IPolicyCmptTypeAttribute policyCmptarttribute = (IPolicyCmptTypeAttribute)attribute;
            if (isAllowedAttribute(policyCmptarttribute)) {
                attributesInDialog.add(policyCmptarttribute);
            }
        }
        if (showSubtypes) {
            if (subtypeHierarchy == null) {
                subtypeHierarchy = policyCmptType.getSubtypeHierarchy();
            }
            List<IType> allSubtypes = subtypeHierarchy.getAllSubtypes(policyCmptType);
            for (IType subtype : allSubtypes) {
                IPolicyCmptType policyCmptTypeSubtype = (IPolicyCmptType)subtype;
                attributes = policyCmptTypeSubtype.getPolicyCmptTypeAttributes();
                for (IAttribute attribute : attributes) {
                    IPolicyCmptTypeAttribute policyCmptarttribute = (IPolicyCmptTypeAttribute)attribute;
                    if (isAllowedAttribute(policyCmptarttribute)) {
                        attributesInDialog.add(policyCmptarttribute);
                    }
                }
            }
        }
        return attributesInDialog.toArray(new IPolicyCmptTypeAttribute[attributesInDialog.size()]);
    }

    /**
     * Only changeable or derived or computed attributes are allowed
     */
    private boolean isAllowedAttribute(IPolicyCmptTypeAttribute attribute) {
        return attribute.isChangeable() || attribute.isDerived();
    }

    protected void setListElements(Object[] elements) {
        Assert.isNotNull(fFilteredList);
        // init the label provider
        AttributeLabelProvider attrLabelProvider = (AttributeLabelProvider)fFilteredList.getLabelProvider();
        attrLabelProvider.setShowPolicyCmptTypeName(showSubtypes);
        fFilteredList.setElements(elements);
        deselectAll();
    }

    /**
     * Creates a filtered list.
     * 
     * @param parent the parent composite.
     * @return returns the filtered list widget.
     */
    protected FilteredList createFilteredList(Composite parent) {
        int flags = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | (fIsMultipleSelection ? SWT.MULTI : SWT.SINGLE);

        FilteredList list = new FilteredList(parent, flags, fRenderer, fIgnoreCase, fAllowDuplicates,
                fMatchEmptyString);

        GridData data = new GridData();
        data.widthHint = convertWidthInCharsToPixels(fWidth);
        data.heightHint = convertHeightInCharsToPixels(fHeight);
        data.grabExcessVerticalSpace = true;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        list.setLayoutData(data);
        list.setFont(parent.getFont());
        list.setFilter((fFilter == null ? "" : fFilter)); //$NON-NLS-1$

        list.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                getContainer().updateButtons();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
        return list;
    }

    protected Text createFilterText(Composite parent) {
        Text text = new Text(parent, SWT.BORDER);

        GridData data = new GridData();
        data.grabExcessVerticalSpace = false;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.BEGINNING;
        text.setLayoutData(data);
        text.setFont(parent.getFont());

        text.setText((fFilter == null ? "" : fFilter)); //$NON-NLS-1$

        Listener listener = $ -> fFilteredList.setFilter(fFilterText.getText());
        text.addListener(SWT.Modify, listener);

        text.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN) {
                    fFilteredList.setFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Nothing to do
            }
        });

        return text;
    }

    private void init(IPolicyCmptType policyCmptType) throws CoreRuntimeException {
        typeHierarchy = policyCmptType.getSupertypeHierarchy();

        AttributeLabelProvider attrLabelProvider = new AttributeLabelProvider();
        attrLabelProvider.setShowPolicyCmptTypeName(showSubtypes);
        fFilteredList.setLabelProvider(attrLabelProvider);

        fFilteredList.setElements(getElements());
        fFilteredList.setSelection((int[])null);
    }

    protected IPolicyCmptTypeAttribute[] getSelection() {
        Assert.isNotNull(fFilteredList);
        Object[] selection = fFilteredList.getSelection();
        IPolicyCmptTypeAttribute[] selectedAttr = new IPolicyCmptTypeAttribute[selection.length];
        System.arraycopy(selection, 0, selectedAttr, 0, selection.length);
        return selectedAttr;
    }

    /**
     * Deselect all elemnts in the attribute list
     */
    public void deselectAll() {
        fFilteredList.setSelection((int[])null);
    }

    /**
     * Check the valid state of the page. Validation errors will be displayed as error inside the
     * wizards message ara.
     */
    public boolean isValid() {
        setErrorMessage(null);
        if (getSelection().length == 0) {
            setErrorMessage(Messages.TestAttributeSelectionWizardPage_errorMessageNothingSelected);
            return false;
        }
        return true;
    }
}
