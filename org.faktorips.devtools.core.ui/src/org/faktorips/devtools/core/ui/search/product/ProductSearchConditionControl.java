/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.ControlComposite;
import org.faktorips.devtools.core.ui.search.product.conditions.ICondition;
import org.faktorips.devtools.core.ui.search.product.conditions.ISearchOperatorType;

public class ProductSearchConditionControl extends ControlComposite {

    private final ProductSearchConditionPresentationModel model;
    private final BindingContext bindingContext = new BindingContext();

    private Combo cboOperatorType;
    private Combo cboObject;
    private final List<EditField<String>> argumentEditFields = new ArrayList<EditField<String>>();

    public ProductSearchConditionControl(Composite parent, ICondition condition,
            ProductSearchPresentationModel parentModel) throws CoreException {
        super(parent, SWT.BORDER);
        model = new ProductSearchConditionPresentationModel(parentModel, condition);

        UIToolkit toolkit = new UIToolkit(null);

        setLayout(new GridLayout(4, false));

        final Button btnDelete = new Button(this, SWT.PUSH);
        btnDelete.setText("X");
        btnDelete.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                model.dispose();
                ProductSearchConditionControl.this.dispose();
                if (!getParent().isDisposed()) {
                    ProductSearchConditionControl.this.getParent().layout();
                    ProductSearchConditionControl.this.getParent().pack();
                }
            }

        });

        cboObject = createObjectCombo(toolkit);

        List<? extends IIpsElement> compareObjects = model.getSearchableElements();
        for (IIpsElement compareObject : compareObjects) {
            // TODO Label statt namen verwenden?
            cboObject.add(compareObject.getName());
        }

        cboOperatorType = toolkit.createCombo(this);
        cboOperatorType.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                model.setOperatorTypeIndex(cboOperatorType.getSelectionIndex());
                updateArgumentFields();
            }
        });

        cboOperatorType.setEnabled(false);
        bindingContext.bindEnabled(cboOperatorType, model,
                ProductSearchConditionPresentationModel.SEARCHED_ELEMENT_CHOSEN);
        updateOperatorTypes();

        setSize(1200, 50);

    }

    protected Combo createObjectCombo(UIToolkit toolkit) {
        final Combo cboObject = toolkit.createCombo(this);

        cboObject.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                model.setSearchedElementIndex(cboObject.getSelectionIndex());
                updateOperatorTypes();
            }
        });
        return cboObject;
    }

    protected void updateOperatorTypes() {
        cboOperatorType.removeAll();
        List<? extends ISearchOperatorType> operatorTypes = model.getSearchOperatorTypes();
        for (ISearchOperatorType searchOperatorType : operatorTypes) {
            cboOperatorType.add(searchOperatorType.getLabel());
        }
    }

    protected void updateArgumentFields() {

        for (EditField<String> argumentControl : argumentEditFields) {
            bindingContext.removeBindings(argumentControl.getControl());
            argumentControl.getControl().dispose();
        }

        ISearchOperatorType operatorType = model.getOperatorType();

        IAttribute attribute = (IAttribute)model.getSearchedElement();
        ValueDatatype valueDatatype = model.getCondition().getValueDatatype(attribute);
        IValueSet valueSet;
        try {
            valueSet = attribute.getValueSet();
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
        IIpsProject ipsProject = attribute.getIpsProject();

        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                valueDatatype);

        for (int i = 0; i < operatorType.getArgumentCount(); i++) {
            EditField<String> editField = controlFactory.createEditField(new UIToolkit(null), this, valueDatatype,
                    valueSet, ipsProject);
            argumentEditFields.add(editField);

            // TODO typesicherheit und weitere argumente
            bindingContext.bindContent(editField, model, ProductSearchConditionPresentationModel.ARGUMENT);
        }

        layout();

    }
}
