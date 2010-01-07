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

package org.faktorips.devtools.core.ui.bf.edit;

import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * This specialization of {@link ActionEditPart} is only necessary because the tabbed property view
 * framework needs different classes to distinguish the kind of objects for which it provides editor
 * views.
 * 
 * @author Peter Erzberger
 */
public class CallMethodActionEditPart extends ActionEditPart {

    public CallMethodActionEditPart() {
        super(IpsUIPlugin.getImageHandling().createImageDescriptor("obj16/CallOperationAction.gif"));
    }

}
