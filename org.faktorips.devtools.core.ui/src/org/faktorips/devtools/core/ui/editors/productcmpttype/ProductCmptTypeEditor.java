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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.ProductCmptTypeDescriptionPage;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeEditor extends TypeEditor implements IModelDescriptionSupport {

    @Override
    protected String getUniformPageTitle() {
        return Messages.ProductCmptTypeEditor_title + getIpsObject().getName();
    }

    @Override
    protected void addAllInOneSinglePage() throws PartInitException {
        addPage(new ProductCmptTypeStructurePage(this, false));
        addPage(new CustomIconPage(this));
    }

    @Override
    protected void addSplittedInMorePages() throws PartInitException {
        addPage(new ProductCmptTypeStructurePage(this, true));
        addPage(new ProductCmptTypeBehaviourPage(this));
        addPage(new CustomIconPage(this));
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        return new ProductCmptTypeDescriptionPage(this);
    }
}
