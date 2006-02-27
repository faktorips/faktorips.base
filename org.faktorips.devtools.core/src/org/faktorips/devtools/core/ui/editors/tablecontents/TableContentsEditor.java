package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.text.DateFormat;

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
        ITableContentsGeneration generation = (ITableContentsGeneration)getPreferredGeneration();
        String title = Messages.TableContentsEditor_title + getTableContents().getName();
        if (generation==null) {
            return title;
        }
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
        return title + Messages.TableContentsEditor_1 + format.format(generation.getValidFrom().getTime());
    }

}

