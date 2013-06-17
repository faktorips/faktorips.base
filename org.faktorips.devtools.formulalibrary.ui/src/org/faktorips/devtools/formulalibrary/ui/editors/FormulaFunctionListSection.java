/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.formulalibrary.ui.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;

/**
 * The <tt>FormulaFunctionListSection</tt> generates an <tt>IpsSection</tt> with a
 * <tt>TableViewer</tt> containing a List of <tt>IFormulaFunction</tt>.
 * <p>
 * Using add and remove buttons, <tt>IFormulaFunction</t>s can be added and removed from the List.
 * 
 * @author HBaagil
 */

public class FormulaFunctionListSection extends IpsSection {

    private TableViewer formulaTableViewer;
    private Table formulaTable;
    private IAction deleteFormulaAction;
    private IAction addFormulaAction;
    private Composite fomulaListComposite;
    private FormulaLibraryPmo formulaLibraryPmo;

    /**
     * Creates a new <tt>FormulaFunctionListSection</tt>.
     * 
     * @param parent The <tt>Composite</tt> this <tt>IpsSection</tt> belongs to.
     * @param toolkit The <tt>UIToolkit</tt> for look and feel controls.
     * @param formulaLibraryPmo The <tt>IpsObjectPartPmo</tt> as presentation model object for
     *            <tt>IFormulaFunctionLibrary</tt>.
     */
    public FormulaFunctionListSection(Composite parent, UIToolkit toolkit, FormulaLibraryPmo formulaLibraryPmo) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        this.formulaLibraryPmo = formulaLibraryPmo;
        initControls();
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        createLeftSection(client);
        getBindingContext().updateUI();
    }

    /**
     * Returns the <tt>TableViewer</tt> of this.
     * 
     * @return TableViewer
     */
    public TableViewer getTableViewer() {
        return formulaTableViewer;
    }

    private void createLeftSection(Composite parent) {
        fomulaListComposite = getToolkit().createGridComposite(parent, 1, false, false);
        GridLayout leftGrid = new GridLayout();
        leftGrid.numColumns = 1;
        fomulaListComposite.setLayout(leftGrid);

        createTable();
    }

    private void createTable() {
        createTableViewer();
        GridData tableGridData = createGridData();
        formulaTable.setLayoutData(tableGridData);
        setContentLabelProvider();
        getBindingContext().bindContent(formulaTableViewer, IFormulaFunction.class, formulaLibraryPmo,
                FormulaLibraryPmo.PROPERTY_SELECTED_FORMULA);
        formulaLibraryPmo.addPropertyChangeListener(new RefreshTableViewer());
    }

    private void setContentLabelProvider() {
        FormulaLibraryLabelProvider formulaLibraryLabelProvider = new FormulaLibraryLabelProvider();
        FormulaLibraryContentProvider tableContentProvider = new FormulaLibraryContentProvider();
        formulaTableViewer.setContentProvider(tableContentProvider);

        final MessageCueLabelProvider message = new MessageCueLabelProvider(formulaLibraryLabelProvider,
                formulaLibraryPmo.getIpsProject());
        formulaTableViewer.setLabelProvider(message);
        formulaTableViewer.setInput(formulaLibraryPmo);
    }

    private void createTableViewer() {
        formulaTableViewer = new TableViewer(fomulaListComposite, SWT.BORDER);
        formulaTable = formulaTableViewer.getTable();
        formulaTable.setHeaderVisible(false);
        formulaTable.setLinesVisible(false);
    }

    private GridData createGridData() {
        GridData tableGridData = new GridData();
        tableGridData.horizontalAlignment = SWT.FILL;
        tableGridData.grabExcessVerticalSpace = true;
        tableGridData.grabExcessHorizontalSpace = true;
        tableGridData.verticalAlignment = SWT.FILL;
        return tableGridData;
    }

    @Override
    protected void populateToolBar(IToolBarManager toolBarManager) {
        createActions();

        toolBarManager.add(addFormulaAction);
        toolBarManager.add(deleteFormulaAction);
    }

    private void createActions() {
        addFormulaAction = new AddFormulaAction(formulaTableViewer, formulaLibraryPmo);
        deleteFormulaAction = new DeleteFormulaAction(formulaTableViewer, formulaLibraryPmo);
    }

    @Override
    public void dispose() {
        super.dispose();
        formulaLibraryPmo.removePropertyChangeListener(new RefreshTableViewer());
    }

    public FormulaLibraryPmo getFormulaLibraryPmo() {
        return formulaLibraryPmo;
    }

    public class RefreshTableViewer implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            formulaTableViewer.refresh();
        }
    }
}
