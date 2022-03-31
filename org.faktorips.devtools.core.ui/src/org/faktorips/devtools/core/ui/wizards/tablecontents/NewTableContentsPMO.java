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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

public class NewTableContentsPMO extends NewProductDefinitionPMO {

    public static final String PROPERTY_SELECTED_STRUCTURE = "selectedStructure"; //$NON-NLS-1$

    public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

    private final NewTableContentsValidator validator;

    private ITableStructure selectedStructure;

    private String name = StringUtils.EMPTY;

    private final List<ITableStructure> structuresList = new ArrayList<>();

    private ITableContentUsage addToTableUsage;

    private boolean autosaveAddToFile;

    public NewTableContentsPMO() {
        validator = new NewTableContentsValidator(this);
    }

    @Override
    protected NewTableContentsValidator getValidator() {
        return validator;
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        updateStructuresList(ipsProject);
        super.setIpsProject(ipsProject);
    }

    /**
     * @param selectedStructure The selectedStructure to set.
     */
    public void setSelectedStructure(ITableStructure selectedStructure) {
        ITableStructure oldStructure = this.selectedStructure;
        this.selectedStructure = selectedStructure;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_STRUCTURE, oldStructure, selectedStructure));
    }

    /**
     * @return Returns the selectedStructure.
     */
    public ITableStructure getSelectedStructure() {
        return selectedStructure;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_NAME, oldName, name));
    }

    /**
     * @return Returns the name.
     */
    @Override
    public String getName() {
        return name;
    }

    private void updateStructuresList(IIpsProject ipsProject) {
        structuresList.clear();
        if (ipsProject == null) {
            return;
        }
        IIpsSrcFile[] findIpsSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
        for (IIpsSrcFile ipsSrcFile : findIpsSrcFiles) {
            structuresList.add((ITableStructure)ipsSrcFile.getIpsObject());
        }
    }

    public List<? extends IIpsObject> getStructuresList() {
        return structuresList;
    }

    public void setAddToTableUsage(ITableContentUsage tableUsage, boolean autosafe) {
        this.autosaveAddToFile = autosafe;
        IIpsProject ipsProject = tableUsage.getIpsProject();
        ITableStructureUsage tableStructureUsage = tableUsage.findTableStructureUsage(ipsProject);
        String[] tableStructures = tableStructureUsage.getTableStructures();
        structuresList.clear();
        for (String structureName : tableStructures) {
            QualifiedNameType qNameType = new QualifiedNameType(structureName, IpsObjectType.TABLE_STRUCTURE);
            IIpsSrcFile tableStructureFile = ipsProject.findIpsSrcFile(qNameType);
            if (tableStructureFile != null) {
                structuresList.add((ITableStructure)tableStructureFile.getIpsObject());
            }
        }
        if (structuresList.size() > 0) {
            setSelectedStructure(structuresList.get(0));
        }
        this.addToTableUsage = tableUsage;
    }

    /**
     * Returns the {@link ITableContentUsage} which holds the target where the new table will be
     * added to.
     */
    public ITableContentUsage getAddToTableUsage() {
        return addToTableUsage;
    }

    /**
     * Returns true if the file where the new table is added to should be saved automatically after
     * being modified
     * 
     * @return true to safe the product component where the table is added to automatically
     */
    public boolean isAutoSaveAddToFile() {
        return autosaveAddToFile;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TABLE_CONTENTS;
    }

}
