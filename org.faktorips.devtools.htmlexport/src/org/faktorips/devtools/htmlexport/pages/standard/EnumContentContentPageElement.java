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

package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

/**
 * A complete page representing an {@link IEnumContent}
 * 
 * @author dicker
 * 
 */
public class EnumContentContentPageElement extends AbstractIpsObjectContentPageElement<IEnumContent> {

    private IEnumType enumType;

    /**
     * 
     * creates a page, which represents the given enumContent according to the given config
     * 
     * @param object
     * @param config
     */
    protected EnumContentContentPageElement(IEnumContent object, DocumentorConfiguration config) {
        super(object, config);
        try {
            this.enumType = object.getIpsProject().findEnumType(object.getEnumType());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.standard.AbstractObjectContentPageElement#build()
     */
    @Override
    public void build() {
        super.build();

        addPageElements(new WrapperPageElement(WrapperType.BLOCK).addPageElements(
                new TextPageElement(IpsObjectType.ENUM_TYPE.getDisplayName() + ": ")).addPageElements( //$NON-NLS-1$
                PageElementUtils.createLinkPageElement(getConfig(), getEnumType(),
                        "content", getEnumType().getQualifiedName(), true))); //$NON-NLS-1$

        addValuesTable();
    }

    /**
     * adds a table with the values of the enumContent
     */
    protected void addValuesTable() {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.EnumContentContentPageElement_values, TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(new EnumValuesTablePageElement(getDocumentedIpsObject()),
                Messages.EnumContentContentPageElement_noValues));

        addPageElements(wrapper);
    }

    /**
     * returns the enumType
     * 
     * @return
     */
    protected IEnumType getEnumType() {
        return enumType;
    }
}
