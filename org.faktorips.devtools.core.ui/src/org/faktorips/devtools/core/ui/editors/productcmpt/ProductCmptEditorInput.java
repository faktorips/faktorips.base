/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

/**
 * Product cmpt editor input based on file editor input. Contains product cmpt relevant information
 * for the product cmpt editor.
 * 
 * @author Joerg Ortmann
 */
public class ProductCmptEditorInput extends FileEditorInput {

    /**
     * Creates a product cmpt editor input with a given generation.<br>
     * Could be used to open a product cmpt and initially showing the given generation.
     */
    public static IFileEditorInput createWithGeneration(IProductCmptGeneration productCmptGeneration) {
        return new ProductCmptEditorInput(productCmptGeneration);
    }

    private final IProductCmptGeneration productCmptGeneration;

    private ProductCmptEditorInput(IFile file) {
        super(file);
        productCmptGeneration = null;
    }

    private ProductCmptEditorInput(IProductCmptGeneration productCmptGeneration) {
        super(productCmptGeneration.getIpsObject().getIpsSrcFile().getCorrespondingFile());
        this.productCmptGeneration = productCmptGeneration;
    }

    /**
     * Returns the {@link IProductCmptGeneration} to be initially opened by the editor or
     * {@code null} if unspecified.
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return productCmptGeneration;
    }

}
