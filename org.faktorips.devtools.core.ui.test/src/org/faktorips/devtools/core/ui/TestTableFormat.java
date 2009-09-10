/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

    public boolean executeEnumExport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        return false;
    }

    public boolean executeEnumImport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) {
        return false;
    }

    public boolean executeTableExport(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        return false;
    }

    public boolean executeTableImport(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public List getImportEnumPreview(IEnumType structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public List getImportTablePreview(ITableStructure structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        return null;
    }

    public boolean isValidImportSource(String source) {
        return false;
    }
}

class TestTableFormatTwo extends TestTableFormat {

}
