/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.bf;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.model.ipsobject.IIpsObject;

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
