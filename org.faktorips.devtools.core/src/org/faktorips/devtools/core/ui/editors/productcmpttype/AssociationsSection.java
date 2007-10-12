/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * A section that shows a product component type's associations in a viewer and 
 * allows to edit association in a dialog, create new associations and delete associations.
 * 
 * @author Jan Ortmann
 */
public class AssociationsSection extends SimpleIpsPartsSection {

    public AssociationsSection(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
        super(pdObject, parent, "Associations", toolkit);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new RelationsComposite(getIpsObject(), parent, toolkit);
    }
    
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }
    
    private class RelationsComposite extends IpsPartsComposite {

        RelationsComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            super(pdObject, parent, true, true, true, true, true, toolkit);
        }

        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                public Object[] getElements(Object inputElement) {
                    return getProductCmptType().getAssociations();
                }

                public void dispose() {
                    
                }

                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                }
                
            };
        }

        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new AssociationEditDialog((IProductCmptTypeAssociation)part, shell);
        }

        protected IIpsObjectPart newIpsPart() {
            return getProductCmptType().newProductCmptTypeAssociation();
        }
        
    }

}
