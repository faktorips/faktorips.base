/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

public class TableStructureEditorStructurePage extends IpsObjectEditorPage {

    static final String PAGE_ID = "SearchStructure"; //$NON-NLS-1$

    private TableStructureContentsChangeListener contentsChangeListener;

    public TableStructureEditorStructurePage(TableStructureEditor editor) {
        super(editor, PAGE_ID, Messages.StructurePage_title);
    }

    TableStructureEditor getTableEditor() {
        return (TableStructureEditor)getEditor();
    }

    ITableStructure getTableStructure() {
        return getTableEditor().getTableStructure();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        super.createPageContent(formBody, toolkit);
        formBody.setLayout(createPageLayout(1, false));
        new GeneralInfoSection(getTableStructure(), formBody, toolkit);
        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new ColumnsSection(getTableStructure(), members, toolkit);
        new IndexSection(getTableStructure(), members, toolkit);
        new RangesSection(getTableStructure(), members, toolkit);
        new ForeignKeysSection(getTableStructure(), members, toolkit);
        updatePageMessage();
        contentsChangeListener = new TableStructureContentsChangeListener(this);
        getTableStructure().getIpsModel().addChangeListener(contentsChangeListener);
    }

    private void updatePageMessage() {
        if (!getManagedForm().getForm().isDisposed()) {
            boolean hasUniqueKeysWithSameDatatype = getTableStructure().hasIndexWithSameDatatype();

            if (hasUniqueKeysWithSameDatatype) {
                getManagedForm().getForm().setMessage(
                        Messages.TableStructureEditorStructurePage_warningUniqueKeysWithSameDatatypes,
                        IMessageProvider.WARNING);
            } else {

                getManagedForm().getForm().setMessage(null, IMessageProvider.NONE);
            }
        }
    }

    @Override
    protected void createToolbarActions(IToolBarManager toolbarManager) {
        toolbarManager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));

        IMenuService menuService = (IMenuService)getSite().getService(IMenuService.class);
        menuService.populateContributionManager((ContributionManager)toolbarManager,
                MenuUtil.toolbarUri(IpsMenuId.TOOLBAR_TABLE_STRUCTURE_EDITOR_PAGE.getId()));
    }

    @Override
    public void dispose() {
        if (getTableStructure() != null) {
            getTableStructure().getIpsModel().removeChangeListener(contentsChangeListener);
        }
        super.dispose();
    }

    private static class TableStructureContentsChangeListener implements ContentsChangeListener {

        private final TableStructureEditorStructurePage page;

        public TableStructureContentsChangeListener(TableStructureEditorStructurePage page) {
            this.page = page;
        }

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            if (event.isAffected(page.getTableStructure())) {
                page.updatePageMessage();
            }
        }
    }
}
