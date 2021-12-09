/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * A control for choosing <code>IEnumType</code>s.
 * <p>
 * The control is configured by the constructor whether to be used to choose a super enumeration
 * type for another <code>IEnumType</code> or to choose an <code>IEnumType</code> for an
 * <code>IEnumContent</code>.
 * <p>
 * If the control is instrumented to be used for choosing a super enumeration type then the
 * currently set <code>IEnumType</code> will not be shown in the list as well as any
 * <code>IEnumType</code>s that subclass it.
 * <p>
 * If the control however is instrumented to be used for choosing an <code>IEnumType</code> for an
 * <code>IEnumContent</code> then abstract <code>IEnumType</code>s and those that contain values are
 * not displayed.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeRefControl extends IpsObjectRefControl {

    /** The current <code>IEnumType</code> for that possible supertypes will be displayed. */
    private IEnumType currentEnumType;

    /** Flag indicating whether this control will be used to select a super enumeration type. */
    private boolean chooseSuperEnumType;

    /**
     * Creates a new <code>EnumTypeRefControl</code>.
     * 
     * @param project The IPS project to search for <code>IEnumType</code>s.
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit to create UI elements with.
     * @param chooseSuperEnumType Flag indicating whether this control will be used to select a
     *            super enumeration type.
     */
    public EnumTypeRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, boolean chooseSuperEnumType) {
        super(project, parent, toolkit, Messages.EnumTypeRefControl_title, Messages.EnumTypeRefControl_description);
        this.chooseSuperEnumType = chooseSuperEnumType;
    }

    /**
     * Sets the current <code>IEnumType</code>. The current <code>IEnumType</code> and its sub types
     * will not be shown in the contents of the control.
     * 
     * @param currentEnumType The current <code>IEnumType</code> for that possible supertypes will
     *            be displayed.
     * 
     * @throws NullPointerException If currentEnumType is <code>null</code>.
     */
    public void setCurrentEnumType(IEnumType currentEnumType) {
        ArgumentCheck.notNull(currentEnumType);
        this.currentEnumType = currentEnumType;
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreRuntimeException {
        List<IIpsSrcFile> resultSrcFiles = new ArrayList<>(0);
        if (getIpsProjects().isEmpty() || getIpsProjects().get(0) == null) {
            return new IIpsSrcFile[0];
        }

        IIpsSrcFile[] ipsSrcFiles = getIpsProjects().get(0).findIpsSrcFiles(IpsObjectType.ENUM_TYPE);
        for (IIpsSrcFile currentIpsSrcFile : ipsSrcFiles) {
            IEnumType currentLoopEnumType = (IEnumType)currentIpsSrcFile.getIpsObject();
            if (currentLoopEnumType != currentEnumType) {
                if (chooseSuperEnumType) {
                    if (!(isSubtypeOfCurrentEnumType(currentLoopEnumType)) && currentLoopEnumType.isAbstract()) {
                        resultSrcFiles.add(currentIpsSrcFile);
                    }
                } else {
                    if (!(currentLoopEnumType.isAbstract()) && currentLoopEnumType.isExtensible()) {
                        resultSrcFiles.add(currentIpsSrcFile);
                    }
                }
            }
        }

        return resultSrcFiles.toArray(new IIpsSrcFile[resultSrcFiles.size()]);
    }

    /**
     * Returns whether the given <code>IEnumType</code> is a subtype of the current
     * <code>IEnumType</code>.
     */
    private boolean isSubtypeOfCurrentEnumType(IEnumType enumType) {
        if (currentEnumType == null) {
            return false;
        }

        IIpsProject ipsProject = enumType.getIpsProject();
        IEnumType currentSuperEnumType = enumType.findSuperEnumType(ipsProject);
        while (currentSuperEnumType != null) {
            if (currentSuperEnumType == currentEnumType) {
                return true;
            }
            currentSuperEnumType = currentSuperEnumType.findSuperEnumType(ipsProject);
        }

        return false;
    }

}
