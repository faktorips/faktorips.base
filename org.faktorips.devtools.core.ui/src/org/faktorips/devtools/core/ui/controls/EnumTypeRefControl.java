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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;

public class EnumTypeRefControl extends IpsObjectRefControl {

    private IEnumType currentEnumType;

    /**
     * Creates a new enum type ref control.
     * 
     * @param project
     * @param parent
     * @param toolkit
     */
    public EnumTypeRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.EnumTypeRefControl_title, Messages.EnumTypeRefControl_description);
        this.currentEnumType = null;
    }

    /**
     * Sets the current enum type. The current enum type and its subclasses will not be shown in the
     * contents of the control.
     * 
     * @param currentEnumType The current enum type.
     * 
     * @throws NullPointerException If currentEnumType is <code>null</code>.
     */
    public void setCurrentEnumType(IEnumType currentEnumType) {
        ArgumentCheck.notNull(currentEnumType);
        this.currentEnumType = currentEnumType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        List<IIpsSrcFile> resultSrcFiles = new ArrayList<IIpsSrcFile>(0);
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }

        IIpsSrcFile[] ipsSrcFiles = getIpsProject().findIpsSrcFiles(IpsObjectType.ENUM_TYPE);
        for (IIpsSrcFile currentIpsSrcFile : ipsSrcFiles) {
            IEnumType currentLoopEnumType = (IEnumType)currentIpsSrcFile.getIpsObject();
            if (currentLoopEnumType != currentEnumType) {
                if (!(isSubtypeOfCurrentEnumType(currentLoopEnumType))) {
                    resultSrcFiles.add(currentIpsSrcFile);
                }
            }
        }

        return resultSrcFiles.toArray(new IIpsSrcFile[resultSrcFiles.size()]);
    }

    // Returns whether the given enum type is a subtype of the current enum type
    private boolean isSubtypeOfCurrentEnumType(IEnumType enumType) throws CoreException {
        if (currentEnumType == null) {
            return false;
        }

        IEnumType currentSuperEnumType = enumType.findSuperEnumType();
        while (currentSuperEnumType != null) {
            if (currentSuperEnumType == currentEnumType) {
                return true;
            }
            currentSuperEnumType = currentSuperEnumType.findSuperEnumType();
        }

        return false;
    }

}
