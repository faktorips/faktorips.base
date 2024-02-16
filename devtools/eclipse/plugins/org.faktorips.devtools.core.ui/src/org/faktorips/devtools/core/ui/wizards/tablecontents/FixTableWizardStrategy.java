/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.fixcontent.AssignContentAttributesPage;
import org.faktorips.devtools.core.ui.wizards.fixcontent.DeltaFixWizardStrategy;
import org.faktorips.devtools.core.ui.wizards.fixcontent.Messages;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

public class FixTableWizardStrategy implements DeltaFixWizardStrategy<ITableStructure, IColumn> {

    private ITableContents tableContents;
    private ITableStructure selectedContentType;

    public FixTableWizardStrategy(ITableContents tableContents) {
        this.tableContents = tableContents;
    }

    @Override
    public ITableContents getContent() {
        return tableContents;
    }

    @Override
    public ITableStructure findContentType(IIpsProject ipsProject, String qName) {
        if (qName == null && selectedContentType == null) {
            selectedContentType = tableContents.findTableStructure(ipsProject);
        } else if (qName != null) {
            selectedContentType = (ITableStructure)ipsProject.findIpsObject(IpsObjectType.TABLE_STRUCTURE, qName);
        }
        return selectedContentType;
    }

    @Override
    public IIpsProject getIpsProject() {
        return tableContents.getIpsProject();
    }

    @Override
    public List<IPartReference> getContentAttributeReferences() {
        return tableContents.getColumnReferences();
    }

    @Override
    public String getContentAttributeReferenceName(List<IPartReference> contentAttributeReferences, int i) {
        return i < contentAttributeReferences.size()
                ? contentAttributeReferences.get(i).getName()
                : tableContents.findTableStructure(getIpsProject()).getColumn(i).getName();
    }

    @Override
    public int getContentAttributeReferencesCount() {
        return tableContents.getNumOfColumns();
    }

    @Override
    public String getContentTypeString() {
        return Messages.TableStructureString;
    }

    @Override
    public IpsObjectRefControl createContentTypeRefControl(UIToolkit uitoolkit, Composite workArea) {
        return uitoolkit.createTableStructureRefControl(getIpsProject(), workArea);
    }

    @Override
    public int getContentAttributesCountIncludeSupertypeCopies(ITableStructure newContentType,
            boolean includeLiteralName) {
        return tableContents.findTableStructure(getIpsProject()).getNumOfColumns();
    }

    @Override
    public List<IColumn> getContentAttributesIncludeSupertypeCopies(ITableStructure newContentType,
            boolean includeLiteralName) {
        return Arrays.asList(selectedContentType.getColumns());
    }

    @Override
    public void createControl(ITableStructure structure, IpsObjectRefControl contentTypeRefControl) {
        if (structure != null) {
            contentTypeRefControl.setText(structure.getQualifiedName());
        }
    }

    @Override
    public boolean checkForCorrectDataType(String currentComboText, int i) {
        if (selectedContentType != null) {
            String datatypeDestination = selectedContentType.getColumn(i).getDatatype();
            if ("String".equals(datatypeDestination)) { //$NON-NLS-1$
                // all datatypes can be written to a String
                return true;
            }
            // need to cut off the prefix of the combo text
            String modifiedComboText = currentComboText
                    .substring(AssignContentAttributesPage.AVAIABLECOLUMN_PREFIX.length());
            IColumn column = selectedContentType.getColumn(modifiedComboText);
            if (column != null) {
                String datatypeSource = column.getDatatype();
                return datatypeDestination.equals(datatypeSource);
            } else {
                // deleted columns can not be validated since their datatype is unknown
                return true;
            }
        }
        return false;
    }
}
