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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;

public class DefinedDatatypesSection extends Buttons {
    private Text valueObjectText;
    private Text enumtypeText;
    private Text javaClassText;
    private Text nullText;
    private Text supportingNameText;
    private List<Datatype> datatypes;

    public DefinedDatatypesSection(IIpsProjectProperties iIpsProjectProperties, Composite parent, boolean canCreate,
            boolean canEdit, boolean canDelete, boolean canMove, boolean showEditButton, UIToolkit toolkit) {
        super(iIpsProjectProperties, parent, canCreate, canEdit, canDelete, canMove, showEditButton, toolkit,
                ExpandableComposite.TITLE_BAR);
        initControls(toolkit);
        setText(Messages.DefinedDatatypes_title);
    }

    public DefinedDatatypesSection(IIpsProjectProperties iIpsProjectProperties, Composite members, UIToolkit toolkit) {
        this(iIpsProjectProperties, members, true, true, true, false, true, toolkit);
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new ArrayContentProvider();
    }

    @Override
    protected EditDialog createEditDialog(Object object, Shell shell) throws CoreException {
        if (object instanceof Datatype) {
            Datatype datatype = (Datatype)object;
            return new DefinedDataypeDialog(datatype, shell, Messages.DefinedDatatypeDialog_title);
        }
        return new DefinedDataypeDialog(null, shell, Messages.DefinedDatatypeDialog_title);
    }

    @Override
    protected IIpsObjectPart newIpsPart() throws CoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void fillViewer() {
        viewer.setInput(iIpsProjectProperties.getDefinedDatatypes());
        // saveDefinedDatatypes();
    }


//    private void setDatatypeInformation(Datatype definedDatatype) {
//        definedDatatype.getJavaClassName();
//        definedDatatype.getQualifiedName();
//        definedDatatype.isValueDatatype();
//        if (definedDatatype.isValueDatatype()) {
//            ValueDatatype valueDatatype = (ValueDatatype)definedDatatype;
//            valueDatatype.isEnum();
//            valueDatatype.hasNullObject();
//            valueDatatype.toString();
//            if (valueDatatype.isEnum()) {
//                EnumDatatype enumDatatype = (EnumDatatype)valueDatatype;
//                enumDatatype.isSupportingNames();
//                String[] enumIds = enumDatatype.getAllValueIds(false);
//                String[] enumValueName = new String[enumIds.length];
//                int i = 0;
//                for (String enumId : enumIds) {
//                    enumValueName[i] = enumDatatype.getValueName(enumId);
//                    i++;
//                }
//            }
//
//        }

//    }

    private void newPart() throws CoreException {
        Datatype newDatatype = null;
        EditDialog dialog = createEditDialog(iIpsProjectProperties, getShell());

        if (valueObjectText.getText().equals("true")) {

        } else if (enumtypeText.getText().equals("true")) {

        }
        // iIpsProjectProperties.addDefinedDatatype(datatype);
    }

    @Override
    public void deleteItem() {
        Datatype selected = getSelectedPart();
        if (selected != null) {
            // iIpsProjectProperties.removeDefinedDatatype(selected);
            fillViewer();
        }

    }

    public final Datatype getSelectedPart() {
        return (Datatype)getSelectedObject();
    }

    public void saveDefinedDatatypes() {
        TableViewer viewer = (TableViewer)getViewer();
        if (viewer.getSelection().isEmpty()) {
            return;
        }
        Object input = viewer.getInput();
        if (input instanceof Datatype[]) {
            Datatype[] inputDatatype = (Datatype[])input;

        }
        return;
    }
}
