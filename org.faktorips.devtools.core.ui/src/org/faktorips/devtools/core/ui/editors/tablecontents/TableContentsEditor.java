/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.TableDescriptionPage;

/**
 * Editor for a table content.
 */
public class TableContentsEditor extends TimedIpsObjectEditor implements IModelDescriptionSupport {

    private ContentPage contentsPage;

    protected ITableContents getTableContents() {
        return (ITableContents)getIpsObject();
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException {
        contentsPage = new ContentPage(this);
        addPage(contentsPage);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);

        // always refresh the table after saving
        // thus all problem markers of unique key error are updated
        // necessary because maybe there are old unique key validation problem marker,
        // this could happen if the unique key error state didn't changed (e.g. at least there are
        // errors)
        contentsPage.refreshTable();
    }

    @Override
    protected String getUniformPageTitle() {
        return Messages.TableContentsEditor_TableContentsEditor_title2
                + " " + getTableContents().getName() + " (" + getTableContents().getTableStructure() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        return new TableDescriptionPage(this);
    }

}
