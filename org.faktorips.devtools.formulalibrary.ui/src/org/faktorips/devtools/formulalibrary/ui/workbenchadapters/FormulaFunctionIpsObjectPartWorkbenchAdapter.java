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

package org.faktorips.devtools.formulalibrary.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.workbenchadapters.DefaultIpsObjectPartWorkbenchAdapter;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;

/**
 * WorkbenchAdapter for the {@link IFormulaFunction}
 * 
 * @author frank
 */
public class FormulaFunctionIpsObjectPartWorkbenchAdapter extends DefaultIpsObjectPartWorkbenchAdapter {

    public FormulaFunctionIpsObjectPartWorkbenchAdapter(ImageDescriptor imageDescriptor) {
        super(imageDescriptor);
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        IFormulaFunction formulaFunction = getFormulaFunctionIpsObjectPart(ipsObjectPart);
        return formulaFunction.getFormulaMethod().getSignatureString() + " : " + formulaFunction.getFormulaMethod().getDatatype(); //$NON-NLS-1$
    }

    private IFormulaFunction getFormulaFunctionIpsObjectPart(IIpsObjectPart ipsObjectPart) {
        return (IFormulaFunction)ipsObjectPart;
    }

}
