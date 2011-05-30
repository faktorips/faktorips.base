/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
