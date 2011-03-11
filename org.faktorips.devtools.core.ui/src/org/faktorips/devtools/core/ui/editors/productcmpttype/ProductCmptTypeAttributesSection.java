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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.AttributesSection;

/**
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttributesSection extends AttributesSection {

    private ProductCmptTypeAttributesComposite attributesComposite;

    public ProductCmptTypeAttributesSection(IpsObjectEditorPage editorPage, IProductCmptType productCmptType,
            Composite parent, UIToolkit toolkit) {

        super(editorPage, productCmptType, parent, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        attributesComposite = new ProductCmptTypeAttributesComposite(getIpsObject(), parent, toolkit);
        return attributesComposite;
    }

    @Override
    protected AttributesComposite getAttributesComposite() {
        return attributesComposite;
    }

    private class ProductCmptTypeAttributesComposite extends AttributesComposite {

        public ProductCmptTypeAttributesComposite(IIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
            super(ipsObject, parent, toolkit);
        }

        @Override
        protected ILabelProvider createLabelProvider() {
            return new DefaultLabelProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IProductCmptTypeAttribute)part, shell);
        }

    }

}
