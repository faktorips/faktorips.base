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

package org.faktorips.devtools.core.ui.editors.enumcontent;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.views.modeldescription.DefaultModelDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.DescriptionItem;
import org.faktorips.util.ArgumentCheck;

/**
 * The model description page to display when an enum content editor is active.
 * 
 * @see EnumCopntentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentModelDescriptionPage extends DefaultModelDescriptionPage implements ContentsChangeListener {

    /** The enum content to display a description for. */
    private IEnumContent enumContent;

    /**
     * Creates the <code>EnumContentModelDescriptionPage</code>.
     * 
     * @throws CoreException If an error occurs while searching for the enum type referenced by the
     *             enum content.
     * @throws NullPointerException If <code>editor</code> is <code>null</code>.
     */
    public EnumContentModelDescriptionPage(EnumContentEditor editor) throws CoreException {
        super();
        ArgumentCheck.notNull(editor);

        enumContent = editor.getEnumContent();

        setDescriptionData();

        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
    }

    /**
     * Sets the current data.
     * 
     * <ul>
     * <li>Sets the title to the name of the enum content.
     * <li>Creates a description item for each enum attribute of the enum type referenced by the
     * enum content.
     * </ul>
     * 
     * @throws CoreException If an error occurs while searching for the enum type referenced by the
     *             enum content.
     */
    private void setDescriptionData() throws CoreException {
        setTitle(enumContent.getName());

        IEnumType enumType = enumContent.findEnumType(enumContent.getIpsProject());
        if (enumType == null) {
            return;
        }

        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
        DescriptionItem[] descriptionItems = new DescriptionItem[enumAttributes.size()];
        for (int i = 0; i < enumAttributes.size(); i++) {
            IEnumAttribute currentEnumAttribute = enumAttributes.get(i);
            DescriptionItem currentItem = new DescriptionItem(currentEnumAttribute.getName(), currentEnumAttribute
                    .getDescription());
            descriptionItems[i] = currentItem;
        }

        setDescriptionItems(descriptionItems);
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        /*
         * Return if the changed ips src file was not the ips src file of the enum type referenced
         * by the enum content or the ips src file of the enum content itself.
         */

        IEnumType enumType = null;
        try {
            enumType = enumContent.findEnumType(enumContent.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        IIpsSrcFile changedIpsSrcFile = event.getIpsSrcFile();
        if (changedIpsSrcFile != enumType.getIpsSrcFile() && changedIpsSrcFile != enumContent.getIpsSrcFile()) {
            return;
        }

        if (enumType != null) {
            try {
                setDescriptionData();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
    }
}
