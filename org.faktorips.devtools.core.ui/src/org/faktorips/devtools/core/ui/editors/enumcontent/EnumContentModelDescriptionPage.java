/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
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
 * The model description page to display when an <tt>EnumContentEditor</tt> is active.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentModelDescriptionPage extends DefaultModelDescriptionPage implements ContentsChangeListener {

    /** The <tt>IEnumContent</tt> to display a description for. */
    private IEnumContent enumContent;

    /**
     * Creates the <tt>EnumContentModelDescriptionPage</tt>.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IEnumType</tt>
     *             referenced by the <tt>IEnumContent</tt>.
     * @throws NullPointerException If <tt>editor</tt> is <tt>null</tt>.
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
     * <ul>
     * <li>Sets the title to the name of the <tt>IEnumContent</tt>.
     * <li>Creates a description item for each <tt>IEnumAttribute</tt> of the <tt>IEnumType</tt>
     * referenced by the <tt>IEnumContent</tt>.
     * </ul>
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IEnumType</tt>
     *             referenced by the <tt>IEnumContent</tt>.
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
            DescriptionItem currentItem = new DescriptionItem(currentEnumAttribute.getName(),
                    currentEnumAttribute.getDescription());
            descriptionItems[i] = currentItem;
        }

        setDescriptionItems(descriptionItems);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        /*
         * Return if the changed IIpsSrcFile was not the IIpsSrcFile of the EnumType referenced by
         * the EnumContent or the IIpsSrcFile of the EnumContent itself.
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

        try {
            setDescriptionData();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
    }

}
