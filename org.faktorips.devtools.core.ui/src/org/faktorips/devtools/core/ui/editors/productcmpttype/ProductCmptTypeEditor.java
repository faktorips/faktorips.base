/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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
        addPage(new CategoryPage(this));
        addPage(new CustomIconPage(this));
    }

    @Override
    protected void addSplittedInMorePages() throws PartInitException {
        addPage(new ProductCmptTypeStructurePage(this, true));
        addPage(new ProductCmptTypeBehaviourPage(this));
        addPage(new CategoryPage(this));
        addPage(new CustomIconPage(this));
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        return new ProductCmptTypeDescriptionPage((IProductCmptType)getIpsObject());
    }

}
