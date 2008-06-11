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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

/**
 * 
 * @author Daniel Hohenberger
 */
public class OpenIpsObjectSelectionDialog extends AbstractElementListSelectionDialog {

    private static final String IPSOBJECTTYPESFILTER_KEY = "IpsObjectTypesFilter"; //$NON-NLS-1$

    private Object[] fElements;

    // Map contains all duplicate elements, this map is used by the label provider
    // to show the project and root if the name is duplicate
    // Remark: the super class needs the label provider on construction time
    // thus we must use a static map to check the duplicates but
    // the dialog will be opened modular, thus a static helper map doesn't matter
    private static Map duplicateNamesMap;
    
    private IpsObjectType[] types;
    private Table filterList;
    
    private ViewForm fForm;
    private CLabel packageInfo;
    
    private IDialogSettings dialogSettingsSection;

    private ExpandableComposite expandableComposite;

    private Composite thisDialogArea;

    public static class DefaultLabelProviderDuplicateSupport extends DefaultLabelProvider {
        public DefaultLabelProviderDuplicateSupport() {
            super(true);
        }

        /**
         * {@inheritDoc}
         */
        public String getText(Object element) {
            boolean isIpsSrcFile = element instanceof IIpsSrcFile;

            String label = super.getText(element);
            if (isDuplicate(getNameFor(element, isIpsSrcFile))) {
                label += " - " + getAdditionalLabel(element, isIpsSrcFile);
            }
            return label;
        }

        private String getNameFor(Object element, boolean isIpsSrcFile) {
            if (isIpsSrcFile) {
                return ((IIpsSrcFile)element).getName();
            } else {
                return ((IIpsObject)element).getName();
            }
        }

        private String getAdditionalLabel(Object element, boolean isIpsSrcFile) {
            IIpsPackageFragment fragment;
            if (isIpsSrcFile) {
                fragment = ((IIpsSrcFile)element).getIpsPackageFragment();
            } else {
                fragment = ((IIpsObject)element).getIpsPackageFragment();
            }
            return getPackageSrcLabel(fragment);
        }
    }
    
    /**
     * Creates a list selection dialog.
     * 
     * @param parent the parent widget.
     * @param renderer the label renderer.
     */
    public OpenIpsObjectSelectionDialog(Shell parent, String dialogTitle, String dialogMessage) {
        super(parent, new DefaultLabelProviderDuplicateSupport());
        setTitle(dialogTitle);
        setMessage(dialogMessage);
        setIgnoreCase(true);
        setMatchEmptyString(true);
        setMultipleSelection(false);
    }

    private IDialogSettings getDialogSettingsSection(String sectionName) {
        IDialogSettings dialogSettings = IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings section = dialogSettings.getSection(sectionName);
        if (section == null){
            section = dialogSettings.addNewSection(sectionName);
        }
        return section;
    }

    /**
     * Sets the elements of the list.
     * 
     * @param elements the elements of the list.
     */
    public void setElements(final Object[] elements) {
        final boolean isIpsObject = elements instanceof IIpsObject[];
        final boolean isIpsSrcFile = elements instanceof IIpsSrcFile[];

        if (isIpsObject || isIpsSrcFile) {
            fElements = elements;
            checkDuplicates(elements, isIpsSrcFile);
        }
    }

    /*
     * Store duplicate objects in a seperate hash, duplicate entries will be displayed with
     * additional package info in their label
     */
    private void checkDuplicates(final Object[] elements, final boolean isIpsSrcFile) {
        duplicateNamesMap = Collections.synchronizedMap(new HashMap(elements.length));
        HashMap map = new HashMap(elements.length);
        for (int i = 0; i < elements.length; i++) {
            String name = getName(elements[i], isIpsSrcFile);
            if (map.get(name) != null) {
                duplicateNamesMap.put(name, name);
            } else {
                map.put(name, name);
            }
        }
    }

    private String getName(Object object, boolean isIpsSrcFile){
        if (isIpsSrcFile){
            return ((IIpsSrcFile) object).getName();
        } else {
            return ((IIpsObject) object).getName();
        }
    }
    
    /*
     * Returns true if the given object name is stored more then once in the list
     */
    private static boolean isDuplicate(String name){
        return duplicateNamesMap != null && duplicateNamesMap.get(name)!=null;
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
            item.setImage(types[i].getEnabledImage());
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
        thisDialogArea = (Composite)super.createDialogArea(parent);

        createMessageArea(thisDialogArea);
        createFilterText(thisDialogArea);
        createTypeList(thisDialogArea);
        createFilteredList(thisDialogArea);
        createPackageInfo(thisDialogArea);
        
        setListElements(fElements);

        setSelection(getInitialElementSelections().toArray());
        
        restoreState();
        
        return thisDialogArea;
    }
    
    private void restoreState() {
        dialogSettingsSection = getDialogSettingsSection("OpenIpsObjectSelectionDialog"); //$NON-NLS-1$
        
        // set size of dialog
        setDialogBoundsSettings(dialogSettingsSection, Dialog.DIALOG_PERSISTSIZE);

        // restore filter
        String[] filters = dialogSettingsSection.getArray(IPSOBJECTTYPESFILTER_KEY);
        if (filters == null){
            return;
        }
        if (filters.length > 0){
            expandableComposite.setExpanded(true);
            updateFilterList();
        }
        
        List selTableItems = new ArrayList(filters.length);
        for (int i = 0; i < filters.length; i++) {
            for (int j = 0; j < filterList.getItemCount(); j++) {
                TableItem item = filterList.getItem(j);
                if (item.getText().equals(filters[i])){
                    selTableItems.add(item);
                    break;
                }
            }
        }
        filterList.setSelection((TableItem[])selTableItems.toArray(new TableItem[selTableItems.size()]));
        handleFilterSelectionChanged();
    }
    
    private void createPackageInfo(Composite contents) {
        fForm = new ViewForm(contents, SWT.BORDER | SWT.FLAT);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        fForm.setLayoutData(gd);
        packageInfo = new CLabel(fForm, SWT.FLAT);
        fForm.setContent(packageInfo);
        packageInfo.setLayoutData(new GridData(GridData.FILL_BOTH));
        fFilteredList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Object[] selection = fFilteredList.getSelection();
                if (selection.length != 1) {
                    packageInfo.setText(""); //$NON-NLS-1$
                    packageInfo.setImage(null);
                } else if (selection[0] instanceof IIpsObject) {
                    IIpsPackageFragment frgmt = ((IIpsObject)selection[0]).getIpsPackageFragment();
                    packageInfo.setText(getPackageSrcLabel(frgmt));
                    packageInfo.setImage(fFilteredList.getLabelProvider().getImage(frgmt));
                } else if (selection[0] instanceof IIpsSrcFile) {
                    IIpsPackageFragment frgmt = ((IIpsSrcFile)selection[0]).getIpsPackageFragment();
                    packageInfo.setText(getPackageSrcLabel(frgmt));
                    packageInfo.setImage(fFilteredList.getLabelProvider().getImage(frgmt));
                }
            }
        });
    }
    
    private static String getPackageSrcLabel(IIpsPackageFragment frgmt){
        String packageSource = frgmt.getName();
        packageSource += " - " + frgmt.getIpsProject().getName() + "/" + frgmt.getRoot().getName(); //$NON-NLS-1$ //$NON-NLS-2$
        return packageSource;
    }
    
    private void createTypeList(Composite contents) {
        expandableComposite = new ExpandableComposite(contents, SWT.NONE);
        expandableComposite.setText(Messages.OpenIpsObjectSelectionDialog_Filter);
        expandableComposite.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                // resizes the application window.
                updateFilterList();
                thisDialogArea.pack(true);
                thisDialogArea.getParent().layout(true);
            }
        });

        filterList = new Table(expandableComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        filterList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event evt) {
                handleFilterSelectionChanged();
            }
        });
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        data.widthHint = convertWidthInCharsToPixels(50);
        data.heightHint = convertHeightInCharsToPixels(5);
        filterList.setLayoutData(data);
        filterList.setFont(contents.getFont());

        data = new GridData(GridData.FILL_HORIZONTAL);
        expandableComposite.setLayoutData(data);
        expandableComposite.setClient(filterList);
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
            IpsObjectType ipsObjectType = null;
            if (fElements[i] instanceof IIpsObject){
                ipsObjectType = ((IIpsObject)fElements[i]).getIpsObjectType();
            } else if (fElements[i] instanceof IIpsSrcFile){
                ipsObjectType = ((IIpsSrcFile)fElements[i]).getIpsObjectType();
            }
            if (ipsObjectType != null) {
                if (activeFilters.contains(ipsObjectType)) {
                    list.add(fElements[i]);
                }
            }
        }
        if (fElements instanceof IIpsObject[]){
            setListElements((IIpsObject[])list.toArray(new IIpsObject[list.size()]));
        } else if (fElements instanceof IIpsSrcFile[]){
            setListElements((IIpsSrcFile[])list.toArray(new IIpsSrcFile[list.size()]));
        }
    }

    public IIpsElement getSelectedObject() {
        if (getResult().length > 0) {
            return (IIpsElement)getResult()[0];
        }
        return null;
    }
    
    public boolean close() {
        // store current filter
        TableItem[] selection = filterList.getSelection();
        String[] filters = new String[selection.length];
        for (int i = 0; i < filters.length; i++) {
            filters[i] = selection[i].getText();
        }
        dialogSettingsSection.put("IpsObjectTypesFilter", filters); //$NON-NLS-1$
        return super.close();
    }
}