/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IPartReference;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.fixcontent.AssignContentAttributesPage;
import org.faktorips.devtools.core.ui.wizards.fixcontent.DeltaFixWizardStrategy;
import org.faktorips.devtools.core.ui.wizards.fixcontent.Messages;

public class FixTableWizardStrategy implements DeltaFixWizardStrategy<ITableStructure, IColumn> {

    private ITableContents tableContents;

    public FixTableWizardStrategy(ITableContents tableContents) {
        this.tableContents = tableContents;
    }

    @Override
    public ITableContents getContent() {
        return tableContents;
    }

    @Override
    public ITableStructure findContentType(IIpsProject ipsProject) {
        try {
            return tableContents.findTableStructure(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
        try {
            return i < contentAttributeReferences.size() ? contentAttributeReferences.get(i).getName()
                    : tableContents.findTableStructure(getIpsProject()).getColumn(i).getName();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
        try {
            return tableContents.findTableStructure(getIpsProject()).getNumOfColumns();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public List<IColumn> getContentAttributesIncludeSupertypeCopies(ITableStructure newContentType,
            boolean includeLiteralName) {
        return Arrays.asList(findContentType(getIpsProject()).getColumns());
    }

    @Override
    public void createControl(ITableStructure structure, IpsObjectRefControl contentTypeRefControl) {
        if (structure != null) {
            contentTypeRefControl.setText(structure.getQualifiedName());
        }
    }

    @Override
    public boolean checkForCorrectDataType(String currentComboText, int i) {
        ITableStructure structure = findContentType(getIpsProject());
        if (structure != null) {
            String datatypeDestination = structure.getColumn(i).getDatatype();
            if ("String".equals(datatypeDestination)) { //$NON-NLS-1$
                // all datatypes can be written to a String
                return true;
            }
            // need to cut off the prefix of the combo text
            String modifiedComboText = currentComboText
                    .substring(AssignContentAttributesPage.AVAIABLECOLUMN_PREFIX.length());
            IColumn column = structure.getColumn(modifiedComboText);
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
