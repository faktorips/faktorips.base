/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;

public class NewTableContentsPMO extends NewProductDefinitionPMO {

    public static final String PROPERTY_SELECTED_STRUCTURE = "selectedStructure"; //$NON-NLS-1$

    public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

    private final NewTableContentsValidator validator;

    private ITableStructure selectedStructure;

    private String name = StringUtils.EMPTY;

    private List<ITableStructure> structuresList = new ArrayList<ITableStructure>();

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
        structuresList = new ArrayList<ITableStructure>();
        if (ipsProject == null) {
            return;
        }
        try {
            IIpsSrcFile[] findIpsSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
            for (IIpsSrcFile ipsSrcFile : findIpsSrcFiles) {
                structuresList.add((ITableStructure)ipsSrcFile.getIpsObject());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public List<? extends IIpsObject> getStructuresList() {
        return structuresList;
    }

}
