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

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;

/**
 * @author Alexander Weickmann
 */
public final class SupportedLanguagesControl extends EditTableControl {

    private List<Locale> locales;

    public SupportedLanguagesControl(Composite parent) {
        super(parent, SWT.NONE);
    }

    @Override
    public void initialize(Object modelObject, String label) {
        super.initialize(modelObject, label);
        initCellEditorsAndConfigureTableViewer();
        setDataChangeable(true);
    }

    public void initialize() {
        initialize(new ArrayList<Locale>(2), null);
    }

    @Override
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
        layouter.addColumnData(new ColumnPixelData(50, false));
        layouter.addColumnData(new ColumnWeightData(90, true));
    }

    @Override
    protected Object addElement() {
        Locale addedLocale = null;

        LocaleSelectionDialog dialog = new LocaleSelectionDialog(getShell(), new HashSet<Locale>(locales));
        int returnCode = dialog.open();
        if (returnCode == LocaleSelectionDialog.OK) {
            addedLocale = dialog.getFirstResult();
            locales.add(addedLocale);
        }
        return addedLocale;
    }

    @Override
    protected UnfocusableTextCellEditor[] createCellEditors() {
        return null;
    }

    @Override
    protected ICellModifier createCellModifier() {
        return null;
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new ContentProvider();
    }

    @Override
    protected ILabelProvider createLabelProvider() {
        return new TableLabelProvider();
    }

    @Override
    protected void createTableColumns(Table table) {
        TableColumn codeColumn = new TableColumn(table, SWT.NONE);
        codeColumn.setResizable(false);
        codeColumn.setText(Messages.SupportedLanguagesControl_columnLanguageCode);

        TableColumn nameColumn = new TableColumn(table, SWT.NONE);
        nameColumn.setResizable(false);
        nameColumn.setText(Messages.SupportedLanguagesControl_columnLanguageName);
    }

    @Override
    protected String[] getColumnPropertyNames() {
        return new String[2];
    }

    @Override
    protected void initModelObject(Object modelObject) {
        locales = (List<Locale>)modelObject;
        locales.add(IpsPlugin.getMultiLanguageSupport().getLocalizationLocale());
    }

    @Override
    protected void removeElement(int index) {
        locales.remove(index);
    }

    @Override
    protected void swapElements(int index1, int index2) {
        Locale locale1 = locales.get(index1);
        locales.set(index1, locales.get(index2));
        locales.set(index2, locale1);
    }

    public List<Locale> getLocales() {
        return locales;
    }

    private class ContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            return locales.toArray();
        }

        @Override
        public void dispose() {
            // Nothing to dispose
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do
        }

    }

    private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            Locale locale = (Locale)element;
            String text = null;
            switch (columnIndex) {
                case 0:
                    text = locale.getLanguage();
                    break;
                case 1:
                    text = locale.getDisplayLanguage();
                    break;
            }
            return text;
        }

    }

}