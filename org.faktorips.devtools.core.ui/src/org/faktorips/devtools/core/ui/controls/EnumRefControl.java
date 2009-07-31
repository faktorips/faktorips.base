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
 * A control for choosing enum types and enum contents.
 * <p>
 * The control is configured by the constructor for a specific IPS project and whether to hide
 * abstract enum types or enum types that contain no values.
 * 
 * @author Roman Grutza
 * 
 * @since 2.3
 */
public class EnumRefControl extends IpsObjectRefControl {

    /** Flag indicating whether to hide abstract enum types. */
    private boolean hideAbstract;

    /** Flag indicating whether to hide enum types that are not containing values. */
    private boolean hideNotContainingValues;

    /**
     * Creates a new <tt>EnumRefControl</tt>.
     * 
     * @param project The IPS project to search for enum types and enum contents.
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit to create UI elements with.
     * @param hideAbstract Flag indicating whether to hide abstract enum types.
     * @param hideNotContainingValues Flag indicating whether to hide enum types that are not
     *            containing values.
     */
    public EnumRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, boolean hideAbstract,
            boolean hideNotContainingValues) {

        super(project, parent, toolkit, Messages.EnumRefControl_title, Messages.EnumRefControl_text);
        this.hideAbstract = hideAbstract;
        this.hideNotContainingValues = hideNotContainingValues;
    }

    /**
     * {@inheritDoc}
     */
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
     * Returns the currently selected enum type or enum content. Returns <tt>null</tt> if no such
     * enum type or enum content exists.
     * 
     * @param priorContent This flag is only relevant if an enum type and an enum content with the
     *            same qualified name exist. If this flag <tt>true</tt> the enum content will be
     *            returned, else the enum type.
     * 
     * @throws CoreException If an error occurs while searching for the selected enum type or enum
     *             content.
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