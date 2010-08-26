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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.actions.IpsAction;

public class ProductEditorDeleteAction extends IpsAction {

    private ProductCmptEditor editor;

    public ProductEditorDeleteAction(ProductCmptEditor editor) {
        super(editor.getSelectionProviderDispatcher());
        this.editor = editor;
    }

    /**
     * Defines which IpsObjectParts can be process by this action. Returns the IpsObjectPart if it
     * can be processed, returns <code>null</code> if the provided object cannot be processed.
     */
    protected IpsObjectPart canBeProcessed(Object selectedIpsObjectPart) {
        if (selectedIpsObjectPart instanceof IProductCmptLink) {
            return (IpsObjectPart)selectedIpsObjectPart;
        }
        return null;
    }

    @Override
    public void dispose() {
        setEnabled(false);
        super.dispose();
    }

    @Override
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        if (!super.computeEnabledProperty(selection)) {
            return false;
        }
        if (!editor.isActiveGenerationEditable()) {
            return false;
        }
        Object[] items = (selection).toArray();
        for (Object item : items) {
            IpsObjectPart part = canBeProcessed(item);
            if (part != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object[] items = selection.toArray();
        for (Object item : items) {
            IpsObjectPart part = canBeProcessed(item);
            if (part != null) {
                part.delete();
            }
        }
    }
}
