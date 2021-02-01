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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A control for choosing <code>IEnumType</code>s and <code>IEnumContent</code>s.
 * <p>
 * The control is configured by the constructor for a specific IPS project and whether to hide
 * abstract <code>IEnumType</code>s or <code>IEnumType</code>s that contain no values.
 * 
 * @author Roman Grutza, Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumRefControl extends IpsObjectRefControl {

    /** Flag indicating whether to hide abstract <code>IEnumType</code>s. */
    private boolean hideAbstract;

    /** Flag indicating whether to hide <code>IEnumType</code>s that are not containing values. */
    private boolean hideNotContainingValues;

    /**
     * Creates a new <code>EnumRefControl</code>.
     * 
     * @param project The IPS project to search for <code>IEnumType</code>s and <code>IEnumContent</code>s.
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit to create UI elements with.
     * @param hideAbstract Flag indicating whether to hide abstract <code>IEnumType</code>s.
     * @param hideNotContainingValues Flag indicating whether to hide <code>IEnumType</code>s that are
     *            not containing values.
     */
    public EnumRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, boolean hideAbstract,
            boolean hideNotContainingValues) {

        super(project, parent, toolkit, Messages.EnumRefControl_title, Messages.EnumRefControl_text);
        this.hideAbstract = hideAbstract;
        this.hideNotContainingValues = hideNotContainingValues;
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }

        IIpsSrcFile[] contents = getIpsProject().findIpsSrcFiles(IpsObjectType.ENUM_CONTENT);
        List<IEnumType> enumTypesList = getIpsProject().findEnumTypes(!hideAbstract, !hideNotContainingValues);
        IIpsSrcFile[] types = new IIpsSrcFile[enumTypesList.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = enumTypesList.get(i).getIpsSrcFile();
        }
        if (contents.length == 0 || types.length == 0) {
            return contents.length == 0 ? types : contents;
        }

        IIpsSrcFile[] result = new IIpsSrcFile[contents.length + types.length];
        for (int i = 0; i < contents.length; i++) {
            result[i] = contents[i];
        }
        for (int i = 0; i < types.length; i++) {
            result[i + contents.length] = types[i];
        }

        return result;
    }

    /**
     * Returns the currently selected <code>IEnumType</code> or <code>IEnumContent</code>. Returns
     * <code>null</code> if no such <code>IEnumType</code> or <code>IEnumContent</code> exists.
     * 
     * @param priorContent This flag is only relevant if an <code>IEnumType</code> and an
     *            <code>IEnumContent</code> with the same qualified name exist. In this scenario, if
     *            this flag <code>true</code> the <code>IEnumContent</code> will be returned, else the
     *            <code>IEnumType</code>.
     * 
     * @throws CoreException If an error occurs while searching for the selected <code>IEnumType</code>
     *             or <code>IEnumContent</code>.
     */
    public IEnumValueContainer findEnum(boolean priorContent) throws CoreException {
        IpsObjectType firstType = priorContent ? IpsObjectType.ENUM_CONTENT : IpsObjectType.ENUM_TYPE;
        IpsObjectType secondType = priorContent ? IpsObjectType.ENUM_TYPE : IpsObjectType.ENUM_CONTENT;
        IEnumValueContainer enumValueContainer = null;
        enumValueContainer = (IEnumValueContainer)getIpsProject().findIpsObject(firstType, getText());
        if (enumValueContainer != null) {
            return enumValueContainer;
        }
        return (IEnumValueContainer)getIpsProject().findIpsObject(secondType, getText());
    }

}
