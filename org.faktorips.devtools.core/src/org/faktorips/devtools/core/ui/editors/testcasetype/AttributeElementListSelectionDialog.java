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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * Select attributes out of a list of policy cmpt type attributes, optional including subtypes of
 * the policy cmpt type.
 * 
 * @author Joerg Ortmann
 */
public class AttributeElementListSelectionDialog extends ElementListSelectionDialog {
    private UIToolkit toolkit;
    private ITypeHierarchy typeHierarchy;
    private IPolicyCmptType policyCmptType;
    private ITypeHierarchy subtypeHierarchy;
    private boolean showSubtypes;
    private Checkbox checkbox;
    
    public AttributeElementListSelectionDialog(Shell parent, UIToolkit toolkit) {
        super(parent, new AttributeLabelProvider());
        this.toolkit = toolkit;
    }

    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent) {
        Composite contents = (Composite) super.createDialogArea(parent);

        checkbox = toolkit.createCheckbox(contents, Messages.AttributeElementListSelectionDialog_ShowAttributesOfSubclasses);
        checkbox.setBackground(contents.getBackground());
        checkbox.getButton().setBackground(contents.getBackground());
        checkbox.setChecked(showSubtypes);
        
        CheckboxField field = new CheckboxField(checkbox);
        field.addChangeListener(new ValueChangeListener(){
            public void valueChanged(final FieldValueChangedEvent e) {
                Runnable runnable = new Runnable(){
                    public void run() {
                        try {
                            showSubtypes = ((CheckboxField)e.field).getCheckbox().isChecked();
                            setListElements(getElements(showSubtypes));
                        } catch (CoreException ex) {
                            IpsPlugin.logAndShowErrorDialog(ex);
                        }
                    }
                };
                BusyIndicator.showWhile(AttributeElementListSelectionDialog.this.getShell().getDisplay(), runnable);
            }
        });
        
        return contents;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void setListElements(Object[] elements) {
        // init the label provider
        if (fFilteredList != null){
            AttributeLabelProvider attrLabelProvider = (AttributeLabelProvider) fFilteredList.getLabelProvider();
            attrLabelProvider.setShowPolicyCmptTypeName(showSubtypes);
        }
        super.setListElements(elements);
    }
    
    private IAttribute[] getElements(boolean showAttrOfSubclasses) throws CoreException{
        IAttribute[] attributes = typeHierarchy.getAllAttributesRespectingOverride(policyCmptType);
        List attributesInDialog = new ArrayList();
        for (int i = 0; i < attributes.length; i++) {
            if (isAllowedAttribute(attributes[i])) {
                attributesInDialog.add(attributes[i]);
            }
        }
        if (showAttrOfSubclasses) {
            if (subtypeHierarchy == null) {
                subtypeHierarchy = policyCmptType.getSubtypeHierarchy();
            }
            IPolicyCmptType[] allSubtypes = subtypeHierarchy.getAllSubtypes(policyCmptType);
            for (int i = 0; i < allSubtypes.length; i++) {
                attributes = allSubtypes[i].getAttributes();
                for (int j = 0; j < attributes.length; j++) {
                    if (isAllowedAttribute(attributes[j])) {
                        attributesInDialog.add(attributes[j]);
                    }
                }
            }
        }
        return (IAttribute[]) attributesInDialog.toArray(new IAttribute[attributesInDialog.size()]);
    }
    
    /*
     * Only changeable or derived or computed attributes are allowed
     */
    private boolean isAllowedAttribute(IAttribute attribute){
        return attribute.isChangeable() || attribute.isDerived();
    }
    
    /**
     * Init dialog with the given policy cmpt type.
     * 
     * @param policyCmptType The policy cmpt type the attributes will be displayed.
     */
    public void init(IPolicyCmptType policyCmptType) throws CoreException{
        this.policyCmptType = policyCmptType;
        
        typeHierarchy = policyCmptType.getSupertypeHierarchy();
        setElements(getElements(showSubtypes));
    }

    /**
     * @param showSubtypes The showSubtypes to set.
     */
    public void setShowSubtypes(boolean showSubtypes) {
        this.showSubtypes = showSubtypes;
    }

    /**
     * @return Returns the showSubtypes.
     */
    public boolean isShowSubtypes() {
        return showSubtypes;
    }
}
