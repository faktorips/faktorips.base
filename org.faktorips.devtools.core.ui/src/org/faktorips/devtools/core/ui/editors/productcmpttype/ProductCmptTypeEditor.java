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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeEditor extends TypeEditor {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUniformPageTitle() {
        return Messages.ProductCmptTypeEditor_title + getIpsObject().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addAllInOneSinglePage() throws PartInitException {
        addPage(new StructurePage(this, false));
        addPage(new CustomIconPage(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSplittedInMorePages() throws PartInitException {
        addPage(new StructurePage(this, true));
        addPage(new BehaviourPage(this));
        addPage(new CustomIconPage(this));
    }
}
