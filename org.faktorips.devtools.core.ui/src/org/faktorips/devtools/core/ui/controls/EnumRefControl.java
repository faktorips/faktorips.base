/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A control for choosing <tt>IEnumType</tt>s and <tt>IEnumContent</tt>s.
 * <p>
 * The control is configured by the constructor for a specific IPS project and whether to hide
 * abstract <tt>IEnumType</tt>s or <tt>IEnumType</tt>s that contain no values.
 * 
 * @author Roman Grutza, Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumRefControl extends IpsObjectRefControl {

    /** Flag indicating whether to hide abstract <tt>IEnumType</tt>s. */
    private boolean hideAbstract;

    /** Flag indicating whether to hide <tt>IEnumType</tt>s that are not containing values. */
    private boolean hideNotContainingValues;

    /**
     * Creates a new <tt>EnumRefControl</tt>.
     * 
     * @param project The IPS project to search for <tt>IEnumType</tt>s and <tt>IEnumContent</tt>s.
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit to create UI elements with.
     * @param hideAbstract Flag indicating whether to hide abstract <tt>IEnumType</tt>s.
     * @param hideNotContainingValues Flag indicating whether to hide <tt>IEnumType</tt>s that are
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
     * Returns the currently selected <tt>IEnumType</tt> or <tt>IEnumContent</tt>. Returns
     * <tt>null</tt> if no such <tt>IEnumType</tt> or <tt>IEnumContent</tt> exists.
     * 
     * @param priorContent This flag is only relevant if an <tt>IEnumType</tt> and an
     *            <tt>IEnumContent</tt> with the same qualified name exist. In this scenario, if
     *            this flag <tt>true</tt> the <tt>IEnumContent</tt> will be returned, else the
     *            <tt>IEnumType</tt>.
     * 
     * @throws CoreException If an error occurs while searching for the selected <tt>IEnumType</tt>
     *             or <tt>IEnumContent</tt>.
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