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
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;

/**
 * A control for choosing <tt>IEnumType</tt>s.
 * <p>
 * The control is configured by the constructor whether to be used to choose a super enumeration
 * type for another <tt>IEnumType</tt> or to choose an <tt>IEnumType</tt> for an
 * <tt>IEnumContent</tt>.
 * <p>
 * If the control is instrumented to be used for choosing a super enumeration type then the
 * currently set <tt>IEnumType</tt> will not be shown in the list as well as any <tt>IEnumType</tt>s
 * that subclass it.
 * <p>
 * If the control however is instrumented to be used for choosing an <tt>IEnumType</tt> for an
 * <tt>IEnumContent</tt> then abstract <tt>IEnumType</tt>s and those that contain values are not
 * displayed.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeRefControl extends IpsObjectRefControl {

    /** The current <tt>IEnumType</tt> for that possible supertypes will be displayed. */
    private IEnumType currentEnumType;

    /** Flag indicating whether this control will be used to select a super enumeration type. */
    private boolean chooseSuperEnumType;

    /**
     * Creates a new <tt>EnumTypeRefControl</tt>.
     * 
     * @param project The IPS project to search for <tt>IEnumType</tt>s.
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
     * Sets the current <tt>IEnumType</tt>. The current <tt>IEnumType</tt> and its sub types will
     * not be shown in the contents of the control.
     * 
     * @param currentEnumType The current <tt>IEnumType</tt> for that possible supertypes will be
     *            displayed.
     * 
     * @throws NullPointerException If currentEnumType is <tt>null</tt>.
     */
    public void setCurrentEnumType(IEnumType currentEnumType) {
        ArgumentCheck.notNull(currentEnumType);
        this.currentEnumType = currentEnumType;
    }

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
                if (chooseSuperEnumType) {
                    if (!(isSubtypeOfCurrentEnumType(currentLoopEnumType)) && currentLoopEnumType.isAbstract()) {
                        resultSrcFiles.add(currentIpsSrcFile);
                    }
                } else {
                    if (!(currentLoopEnumType.isAbstract()) && !(currentLoopEnumType.isContainingValues())) {
                        resultSrcFiles.add(currentIpsSrcFile);
                    }
                }
            }
        }

        return resultSrcFiles.toArray(new IIpsSrcFile[resultSrcFiles.size()]);
    }

    /** Returns whether the given <tt>IEnumType</tt> is a subtype of the current <tt>IEnumType</tt>. */
    private boolean isSubtypeOfCurrentEnumType(IEnumType enumType) throws CoreException {
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
