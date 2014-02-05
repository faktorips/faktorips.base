/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class FixDifferencesCompositeWorkbenchAdapter implements IWorkbenchAdapter {

    @Override
    public Object[] getChildren(Object o) {
        if (o instanceof IFixDifferencesComposite) {
            IFixDifferencesComposite fixDifferencesComposite = (IFixDifferencesComposite)o;
            return fixDifferencesComposite.getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        if (object instanceof IFixDifferencesComposite) {
            IFixDifferencesComposite fixDifferencesComposite = (IFixDifferencesComposite)object;
            return IpsUIPlugin.getImageHandling().getImageDescriptor(
                    fixDifferencesComposite.getCorrespondingIpsElement());
        }
        return null;
    }

    @Override
    public String getLabel(Object o) {
        if (o instanceof IFixDifferencesComposite) {
            IFixDifferencesComposite fixDifferencesComposite = (IFixDifferencesComposite)o;
            if (fixDifferencesComposite.getCorrespondingIpsElement() instanceof ILabeledElement) {
                ILabeledElement labeledElement = (ILabeledElement)fixDifferencesComposite.getCorrespondingIpsElement();
                return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(labeledElement);
            } else {
                return fixDifferencesComposite.getCorrespondingIpsElement().getName();
            }
        }
        return null;
    }

    @Override
    public Object getParent(Object o) {
        // do not know the parent
        return null;
    }

}
