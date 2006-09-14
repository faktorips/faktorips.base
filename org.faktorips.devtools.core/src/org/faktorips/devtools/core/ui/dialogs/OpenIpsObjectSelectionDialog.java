/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.AbstractElementListSelectionDialog;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

/**
 * 
 * @author Daniel Hohenberger
 */
public class OpenIpsObjectSelectionDialog extends AbstractElementListSelectionDialog {

    private Object[] fElements;
    private IpsObjectType[] types;
    private Table filterList;

    /**
     * Creates a list selection dialog.
     * 
     * @param parent the parent widget.
     * @param renderer the label renderer.
     */
    public OpenIpsObjectSelectionDialog(Shell parent, String dialogTitle, String dialogMessage) {
        super(parent, new DefaultLabelProvider());
        setTitle(dialogTitle);
        setMessage(dialogMessage);
        setIgnoreCase(true);
        setMatchEmptyString(true);
        setMultipleSelection(false);
    }

    /**
     * Sets the elements of the list.
     * 
     * @param elements the elements of the list.
     */
    public void setElements(Object[] elements) {
        if (elements instanceof IIpsObject[]) {
            fElements = elements;
        }
    }

    /**
     * Sets the types of the filter list.
     * 
     * @param types the types of the filter list.
     */
    public void setTypes(IpsObjectType[] types) {
        this.types = types;
    }

    private void updateFilterList() {
        filterList.removeAll();
        for (int i = 0; i < types.length; i++) {
            TableItem item = new TableItem(filterList, SWT.NONE);
            item.setText(types[i].getName());
            item.setImage(types[i].getImage());
        }
        handleFilterSelectionChanged();
    }

    /*
     * @see SelectionStatusDialog#computeResult()
     */
    protected void computeResult() {
        setResult(Arrays.asList(getSelectedElements()));
    }

    /*
     * @see Dialog#createDialogArea(Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite contents = (Composite)super.createDialogArea(parent);

        createMessageArea(contents);
        createFilterText(contents);
        createTypeList(contents);
        createFilteredList(contents);

        setListElements(fElements);

        setSelection(getInitialElementSelections().toArray());

        return contents;
    }

    private void createTypeList(Composite contents) {

        ExpandableComposite composite = new ExpandableComposite(contents, SWT.NONE);
        composite.setText("Filter");
        composite.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                // resizes the application window.
                updateFilterList();
                getShell().pack(true);
            }
        });

        filterList = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        filterList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event evt) {
                handleFilterSelectionChanged();
            }
        });
        filterList.addListener(SWT.MouseDoubleClick, new Listener() {
            public void handleEvent(Event evt) {
                handleDefaultSelected();
            }
        });
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        data.widthHint = convertWidthInCharsToPixels(50);
        data.heightHint = convertHeightInCharsToPixels(5);
        filterList.setLayoutData(data);
        filterList.setFont(contents.getFont());

        data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);
        composite.setClient(filterList);
    }

    protected void handleFilterSelectionChanged() {
        int[] selection = filterList.getSelectionIndices();
        if (selection.length == 0) {
            setListElements(fElements);
            return;
        }
        Set activeFilters = new HashSet(selection.length);
        for (int i = 0; i < selection.length; i++) {
            int s = selection[i];
            activeFilters.add(types[s]);
        }
        List list = new ArrayList();
        for (int i = 0; i < fElements.length; i++) {
            IIpsObject object = (IIpsObject)fElements[i];
            if (activeFilters.contains(object.getIpsObjectType())) {
                list.add(object);
            }
        }
        setListElements((IIpsObject[])list.toArray(new IIpsObject[list.size()]));
    }

    public IIpsObject getSelectedObject() {
        if (getResult().length > 0) {
            return (IIpsObject)getResult()[0];
        }
        return null;
    }
}