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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.type.MethodsSection;

public class PolicyCmptTypeBehaviourPage extends PolicyCmptTypeEditorPage {

    public PolicyCmptTypeBehaviourPage(PolicyCmptTypeEditor editor) {
        super(editor, true, Messages.BehaviourPage_title, "PolicyCmptTypeBehaviourPage"); //$NON-NLS-1$
        setNumberlayoutColumns(2);
    }

    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        new MethodsSection((IType)getIpsObject(), parentContainer, toolkit);
        new RulesSection((IPolicyCmptType)getIpsObject(), parentContainer, toolkit);
    }

    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        new MethodsSection((IType)getIpsObject(), parentContainer, toolkit);
        new RulesSection((IPolicyCmptType)getIpsObject(), parentContainer, toolkit);
    }

    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {
        // Nothing to do.
    }

}
