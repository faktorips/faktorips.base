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
 * 
 * @author Jan Ortmann
 */
public class RelationsSection extends SimpleIpsPartsSection {

    public RelationsSection(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
        super(pdObject, parent, "Relations", toolkit);
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
    
    /**
     * A composite that shows a policy component's relations in a viewer and 
     * allows to edit relations in a dialog, create new relations and delete relations.
     */
    private class RelationsComposite extends IpsPartsComposite {

        RelationsComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            // create default buttons without the new button, 
            //   because the new button will be overridden with wizard functionality
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
            return new RelationEditDialog((IProductCmptTypeAssociation)part, shell);
        }

        protected IIpsObjectPart newIpsPart() {
            return getProductCmptType().newAssociation();
        }
        
    }

}
