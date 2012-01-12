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

import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.wizards.productdefinition.FolderAndPackagePage;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionWizard;

public class NewTableContentsWizard extends NewProductDefinitionWizard {

    public static final String ID = "newTableContentsWizard"; //$NON-NLS-1$

    public NewTableContentsWizard() {
        super(new NewTableContentsPMO());
    }

    @Override
    protected NewTableContentsPMO getPmo() {
        return (NewTableContentsPMO)super.getPmo();
    }

    @Override
    public void addPages() {
        addPage(new TableContentsPage(getPmo()));
        addPage(new FolderAndPackagePage(getPmo()));
    }

    @Override
    protected String getDialogId() {
        return ID;
    }

    @Override
    protected void initDefaults(IIpsPackageFragment selectedPackage, IIpsObject selectedIpsObject) {
        getPmo().setIpsProject(selectedPackage.getIpsProject());
        getPmo().setPackageRoot(selectedPackage.getRoot());
        getPmo().setIpsPackage(selectedPackage);
        if (selectedIpsObject instanceof ITableStructure) {
            getPmo().setSelectedStructure((ITableStructure)selectedIpsObject);
        } else if (selectedIpsObject instanceof ITableContents) {
            ITableContents tableContent = (ITableContents)selectedIpsObject;
            try {
                getPmo().setSelectedStructure(tableContent.findTableStructure(tableContent.getIpsProject()));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    @Override
    protected IpsObjectType getIpsObjectType() {
        return IpsObjectType.TABLE_CONTENTS;
    }

    @Override
    protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException {
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (ipsObject instanceof ITableContents) {
            ITableContents table = (ITableContents)ipsObject;
            table.setTableStructure(getPmo().getSelectedStructure().getQualifiedName());
            GregorianCalendar date = getPmo().getEffectiveDate();
            IIpsObjectGeneration generation = table.newGeneration();
            generation.setValidFrom(date);
            ITableStructure structure = getPmo().getSelectedStructure();
            if (structure != null) {
                for (int i = 0; i < structure.getNumOfColumns(); i++) {
                    table.newColumn(StringUtils.EMPTY);
                }
            }
        } else {
            throw new RuntimeException("Invalid object type created"); //$NON-NLS-1$
        }
    }

    @Override
    protected void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
        // TODO Auto-generated method stub
    }

}
