/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IDataChangeableReadAccess;
import org.faktorips.devtools.core.ui.IDataChangeableStateChangeListener;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

public class ValidationRuleConfigSection extends IpsSection {

    private static final String ID = "org.faktorips.devtools.core.ui.editors.productcmpt.ValidationRuleSection"; //$NON-NLS-1$

    /** Generation which holds the informations to display */
    private IProductCmptGeneration generation;

    /** Label which is displayed if no validation rules are defined. */
    private Label noRulesLabel;

    private CheckboxTableViewer tableViewer;

    public ValidationRuleConfigSection(final IProductCmptGeneration generation, Composite parent, UIToolkit toolkit) {
        super(ID, parent, GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(generation);
        this.generation = generation;

        setInitCollapsedIfNoContent(true);
        initControls();

        IpsPlugin.getDefault().getIpsModel().addChangeListener(new ContentsChangeListener() {

            @Override
            public void contentsChanged(ContentChangeEvent event) {
                if (event.isAffected(generation)) {
                    setCheckedState();
                }
            }
        });
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);

        Composite composite = createVRuleComposite(client);
        GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
        composite.setLayoutData(gd);
        // following line forces the paint listener to draw a light grey border around
        // the text control. Can only be understood by looking at the FormToolkit.PaintBorder class.
        composite.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(composite);
    }

    @Override
    protected boolean hasContentToDisplay() {
        return generation.getNumOfValidationRules() > 0;
    }

    @Override
    protected String getSectionTitle() {
        return Messages.ValidationRuleSection_DefaultTitle;
    }

    private Composite createVRuleComposite(Composite parent) {
        if (!hasContentToDisplay()) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout());
            noRulesLabel = new Label(composite, SWT.NONE);
            noRulesLabel.setText(Messages.ValidationRuleSection_NoRulesDefinedLabelText);
            noRulesLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, true));
            return composite;
        } else {
            Table table = new Table(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL);
            tableViewer = new CheckboxTableViewer(table);
            tableViewer.setContentProvider(createContentProvider());
            tableViewer.setLabelProvider(createLabelProvider());
            tableViewer.setInput(generation);
            setCheckedState();
            addCheckClickListener();
            setTableEnabled(isDataChangeable());
            addDataChangeableStateChangeListener(new IDataChangeableStateChangeListener() {
                @Override
                public void dataChangeableStateHasChanged(IDataChangeableReadAccess object) {
                    setTableEnabled(object.isDataChangeable());
                }
            });
            return table;
        }

    }

    private void setTableEnabled(boolean dataChangeable) {
        if (tableViewer != null) {
            tableViewer.getTable().setEnabled(dataChangeable);
        }
    }

    private void addCheckClickListener() {
        tableViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getElement() instanceof IValidationRuleConfig) {
                    IValidationRuleConfig config = (IValidationRuleConfig)event.getElement();
                    if (config != null) {
                        config.setActive(!config.isActive());
                    }
                }
            }
        });
    }

    private void setCheckedState() {
        if (tableViewer != null && !tableViewer.getTable().isDisposed()) {
            List<IValidationRuleConfig> configList = generation.getValidationRuleConfigs();
            for (IValidationRuleConfig ruleConfig : configList) {
                tableViewer.setChecked(ruleConfig, ruleConfig.isActive());
            }
        }
    }

    private IContentProvider createContentProvider() {
        return new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing to do
            }

            @Override
            public void dispose() {
                // nothing to do
            }

            @Override
            public Object[] getElements(Object inputElement) {
                IProductCmptGeneration gen = (IProductCmptGeneration)inputElement;
                return gen.getValidationRuleConfigs().toArray();
            }
        };
    }

    private IBaseLabelProvider createLabelProvider() {
        return new DefaultLabelProvider();
    }

    @Override
    protected void performRefresh() {
        /*
         * vViewer may be null if no rules are defined. In that case a composite with a label was
         * created.
         */
        if (tableViewer != null) {
            tableViewer.refresh();
        }
    }

}
