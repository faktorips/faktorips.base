/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.MethodsSection;

/**
 * @author Jan Ortmann
 */
public class ProductCmptTypeMethodsSection extends MethodsSection {

    public ProductCmptTypeMethodsSection(IProductCmptType type, Composite parent, IWorkbenchPartSite site,
            UIToolkit toolkit) {

        super(type, parent, site, toolkit);
        setText(Messages.ProductCmptTypeMethodsSection_title);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new ProductCmptTypeMethodComposite(getType(), parent, getSite(), toolkit);
    }

    private static class ProductCmptTypeMethodComposite extends MethodsComposite {

        public ProductCmptTypeMethodComposite(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
            super(type, parent, site, toolkit);
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new ProductCmptTypeMethodEditDialog((IProductCmptTypeMethod)part, shell);
        }
    }

}
