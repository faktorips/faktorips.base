/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
import org.faktorips.util.message.MessageList;

class TestTableFormat extends AbstractExternalTableFormat {

    @Override
    public boolean executeEnumExport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        return false;
    }

    @Override
    public void executeEnumImport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) {
    }

    @Override
    public boolean executeTableExport(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        return false;
    }

    @Override
    public void executeTableImport(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) {
    }

    @Override
    public List<String[]> getImportEnumPreview(IEnumType structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        return null;
    }

    @Override
    public List<String[]> getImportTablePreview(ITableStructure structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        return null;
    }

    @Override
    public boolean isValidImportSource(String source) {
        return false;
    }
}

class TestTableFormatTwo extends TestTableFormat {

}
