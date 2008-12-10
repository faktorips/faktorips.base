/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;


/**
 * Editor for a table content.
 */
public class TableContentsEditor extends TimedIpsObjectEditor implements IModelDescriptionSupport {

    /**
     *
     */
    public TableContentsEditor() {
        super();
    }

    protected ITableContents getTableContents() {
        return (ITableContents)getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    protected void addPagesForParsableSrcFile() throws PartInitException {
        addPage(new ContentPage(this));
    }

    /**
     * {@inheritDoc}
     */
    protected String getUniformPageTitle() {
    	return Messages.TableContentsEditor_TableContentsEditor_title2 + " " + getTableContents().getName(); //$NON-NLS-1$
    	/* pk: We need a general concept for historical changes of table contents see flyspray entry 131
	        ITableContentsGeneration generation = (ITableContentsGeneration) getPreferredGeneration();
			String gen = IpsPlugin.getDefault().getIpsPreferences()
					.getChangesOverTimeNamingConvention()
					.getGenerationConceptNameSingular(Locale.getDefault());
			DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
			String title = NLS
					.bind(
							Messages.TableContentsEditor_title,
							new Object[] {
									getTableContents().getName(),
									gen,
									generation == null ? "" : format.format(generation.getValidFrom().getTime()) }); //$NON-NLS-1$
			return title;
		*/
    }

	public IPage createModelDescriptionPage() throws CoreException {

		ITableContents tableContents = getTableContents();

        if (tableContents == null) {
            return null;
        }

        TableModelDescriptionPage fModelDescriptionPage = new TableModelDescriptionPage(tableContents);

		return fModelDescriptionPage;
	}

}

