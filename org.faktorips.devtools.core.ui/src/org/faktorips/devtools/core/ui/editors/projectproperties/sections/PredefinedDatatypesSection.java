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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;

public class PredefinedDatatypesSection extends Buttons {
    protected String[] predefinedDatatypesUsed;
    private ArrayList<String> deletedPredefinedDatatype = new ArrayList<String>();

    public PredefinedDatatypesSection(IIpsProjectProperties iIpsProjectProperties, Composite parent, boolean canCreate,
            boolean canEdit, boolean canDelete, boolean canMove, boolean showEditButton, UIToolkit toolkit) {
        super(iIpsProjectProperties, parent, canCreate, canEdit, canDelete, canMove, showEditButton, toolkit,
                ExpandableComposite.TITLE_BAR);
        this.predefinedDatatypesUsed = iIpsProjectProperties.getPredefinedDatatypesUsed();
        initControls(toolkit);
        setText(Messages.PredefinedDatatypes_title);
    }

    public PredefinedDatatypesSection(IIpsProjectProperties iIpsProjectProperties, Composite members, UIToolkit toolkit) {
        this(iIpsProjectProperties, members, true, false, true, false, false, toolkit);
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new ArrayContentProvider();
    }

    @Override
    protected EditDialog createEditDialog(Object object, Shell shell) throws CoreException {
        return new PredefinedDataypeDialog(shell, Messages.PredefinedDataypeDialog_title, deletedPredefinedDatatype);
    }

    @Override
    protected IIpsObjectPart newIpsPart() throws CoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void fillViewer() {
        viewer.setInput(predefinedDatatypesUsed);
    }

    @Override
    public void deleteItem() {
        String selection = getSelectedPart();
        String[] newPredefinedDatatype = new String[predefinedDatatypesUsed.length - 1];
        int i = 0;
        for (String predefinedDatatype : predefinedDatatypesUsed) {
            if (!predefinedDatatype.equals(selection)) {
                newPredefinedDatatype[i] = predefinedDatatype;
                i++;
            } else {
                deletedPredefinedDatatype.add(selection);
            }
        }
        predefinedDatatypesUsed = newPredefinedDatatype;
        fillViewer();
    }

    public final String getSelectedPart() {
        return (String)getSelectedObject();
    }

    public void savePredefinedDatatypes() {
        iIpsProjectProperties.setPredefinedDatatypesUsed(predefinedDatatypesUsed);
    }
}
