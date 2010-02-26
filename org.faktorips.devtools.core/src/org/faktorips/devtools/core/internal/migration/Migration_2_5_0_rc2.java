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

package org.faktorips.devtools.core.internal.migration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Migration from version 2.5.0.rc1 to version 2.6.0.rc1
 * 
 * @author Peter Kuntz
 */
public class Migration_2_5_0_rc2 extends AbstractIpsProjectMigrationOperation {

    public Migration_2_5_0_rc2(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "WARNING do not run this migration: in progress, executing this migration will result in a runtime exception!\n"
                + "The modeling of composition has changed.\n"
                + "Now the child model objects becomes a concrete parent model object variable for each parent they belongs to";
    }

    @Override
    public String getTargetVersion() {
        return "2.6.0.rc1"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        // TODO - set to false when migration is implemented
        return true;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        IIpsProject ipsProject = getIpsProject();
        List<IIpsSrcFile> allIpsSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.collectAllIpsSrcFilesOfSrcFolderEntries(allIpsSrcFiles);
        for (IIpsSrcFile currentIpsSrcFile : allIpsSrcFiles) {
            if (currentIpsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)) {
                IPolicyCmptType policyCmptType = (IPolicyCmptType)currentIpsSrcFile.getIpsObject();
                migratePolicyCmptType(policyCmptType);
            }
        }
        throw new RuntimeException("TODO: not yet implemented!");
    }

    private void migratePolicyCmptType(IPolicyCmptType policyCmptType) {

    }
}
