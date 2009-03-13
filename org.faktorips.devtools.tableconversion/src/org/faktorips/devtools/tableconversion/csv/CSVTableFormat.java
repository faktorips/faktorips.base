package org.faktorips.devtools.tableconversion.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.wizards.tableimport.CSVPropertyComposite;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
import org.faktorips.util.message.MessageList;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Table format for comma separated values (CSV).
 * 
 * @author Roman Grutza
 */
public class CSVTableFormat extends AbstractExternalTableFormat {

    // TODO: init in ctor!
    private boolean ignoreColumnHeaderRow;

    /**
     * {@inheritDoc}
     */
    public IWorkspaceRunnable getExportTableOperation(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        
        return new CSVTableExportOperation(contents, filename.toOSString(), this, nullRepresentationString,
                exportColumnHeaderRow, list);
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
        
        if (! file.canRead()) {
            return false;
        }
        
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file));
            return hasConstantNumberOfFieldsPerLine(reader);
        } catch (Exception e) {
            // ignore FileNotFoundException, IOException
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // this is a serious problem, so report it.
                    IpsPlugin.log(e);
                }
            }
        }
    }

    public List getImportTablePreview(ITableStructure structure, IPath filename, int maxNumberOfRows) {
        if (filename == null ||  ! isValidImportSource(filename.toOSString())) {
            return Collections.EMPTY_LIST;
        }

        List result = new ArrayList();
        MessageList ml = new MessageList();
        CSVReader reader = null;
        try {
            Datatype[] datatypes = getDatatypes(structure);
            
            reader = new CSVReader(new FileReader(filename.toOSString()));
            String[] line = (ignoreColumnHeaderRow == true) ? reader.readNext() : null;
            int linesLeft = maxNumberOfRows;
            while ((line = reader.readNext()) != null) {
                if (linesLeft-- <= 0) {
                    break;
                }
                String[] convertedLine = new String[line.length];
                for (int i = 0; i < line.length; i++) {
                    Object obj = getExternalValue(line[i], datatypes[i], ml);
                    convertedLine[i] = getIpsValue(obj, datatypes[i], ml);
                }
                
                result.add(convertedLine);
            }
        } catch (Exception e) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ee) {
                    // serious problem, report
                    IpsPlugin.log(ee);
                }
            }
        }
        
        return result;
    }

    private boolean hasConstantNumberOfFieldsPerLine(CSVReader reader) throws IOException {
        String[] row = reader.readNext();
        int expectedNumberOfFields = row.length;
        while ((row = reader.readNext()) != null) {
            if (expectedNumberOfFields != row.length) {
                return false;
            }
        }
        return true;
    }    

    /**
     * {@inheritDoc}
     */
    // TODO: move to TableFormatPlugin, see comment in superclass
    public Composite createTableFormatConfigurationControl(Composite parent) {
        return new CSVPropertyComposite(parent);
    }

    
    // TODO: Duplicated code in CSVTableImportOperation
    private Datatype[] getDatatypes(ITableStructure structure) throws CoreException {
        IColumn[] columns = structure.getColumns();
        Datatype[] datatypes = new Datatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
        }
        return datatypes;
    }
}
