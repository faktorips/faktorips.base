package org.faktorips.devtools.extsystems.csv;

import java.io.File;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Table format for comma separated values (CSV).
 * 
 * @author Roman Grutza
 */
public class CSVTableFormat extends AbstractExternalTableFormat {

    /**
     * {@inheritDoc}
     */
    public IWorkspaceRunnable getExportTableOperation(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        
        return new CSVTableExportOperation(contents, filename.toOSString(), this, nullRepresentationString,
                exportColumnHeaderRow, list/*, ";", ""*/);
    }

    /**
     * {@inheritDoc}
     */
    public IWorkspaceRunnable getImportTableOperation(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list) {
        
        return new CSVTableImportOperation(structure, filename.toOSString(), targetGeneration, this,
                nullRepresentationString, ignoreColumnHeaderRow, list);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValidImportSource(String source) {
        File file = new File(source);

        if (file.canRead()) {
            return true;
        }

        return false;
    }

}
