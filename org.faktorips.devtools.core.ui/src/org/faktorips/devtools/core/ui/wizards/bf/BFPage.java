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

package org.faktorips.devtools.core.ui.wizards.bf;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

/**
 * The wizard page for the new business function wizard.
 * 
 * @author Peter Erzberger
 */
public class BFPage extends IpsObjectPage {

    public BFPage(IStructuredSelection selection) {
        super(BusinessFunctionIpsObjectType.getInstance(), selection, Messages.BFPage_Title);
        setImageDescriptor(IpsUIPlugin.getImageHandling()
                .createImageDescriptor("wizards/NewBusinessFunctionWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreException {

        IBusinessFunction bf = (IBusinessFunction)newIpsObject;
        bf.newStart(new Point(200, 20));
        bf.newEnd(new Point(200, 120));
        IControlFlow cf = bf.newControlFlow();
        cf.setSource(bf.getStart());
        cf.setTarget(bf.getEnd());
    }

}
