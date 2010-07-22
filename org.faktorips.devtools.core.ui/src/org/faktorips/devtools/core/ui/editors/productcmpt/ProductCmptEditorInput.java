/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
    private boolean ignoreWorkingDateMissmatch;

    /**
     * Creates a product cmpt editor input with a given generation.<br>
     * Could be used to open a product cmpt and initially showing the given generation.
     */
    public static IFileEditorInput createWithGeneration(IProductCmptGeneration productCmptGeneration) {
        return new ProductCmptEditorInput(productCmptGeneration.getIpsObject().getIpsSrcFile().getCorrespondingFile(),
                true);
    }

    private ProductCmptEditorInput(IFile file, boolean ignoreWorkingDateMissmatch) {
        super(file);
        this.ignoreWorkingDateMissmatch = ignoreWorkingDateMissmatch;
    }

    /**
     * Returns <code>true</code> if a mismatch of the working date should be ignored by the editor
     * (e.g. no mismatch dialog will be shown).
     */
    public boolean isIgnoreWorkingDateMissmatch() {
        return ignoreWorkingDateMissmatch;
    }
}
