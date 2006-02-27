package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.text.DateFormat;
import java.util.Locale;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;


/**
 *
 */
public class TableContentsEditor extends TimedIpsObjectEditor {

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
     * Overridden method.
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
            addPage(new ContentPage(this));
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditor#getUniformPageTitle()
     */
    protected String getUniformPageTitle() {
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
    }

}

