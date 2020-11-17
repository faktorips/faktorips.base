/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.type.MethodsSection;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IType;

public class PolicyCmptTypeBehaviourPage extends PolicyCmptTypeEditorPage {

    public PolicyCmptTypeBehaviourPage(PolicyCmptTypeEditor editor) {
        super(editor, true, Messages.BehaviourPage_title, "PolicyCmptTypeBehaviourPage"); //$NON-NLS-1$
        setNumberlayoutColumns(2);
    }

    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        new MethodsSection((IType)getIpsObject(), parentContainer, getSite(), toolkit);
        new RulesSection((IPolicyCmptType)getIpsObject(), parentContainer, getSite(), toolkit);
    }

    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        new MethodsSection((IType)getIpsObject(), parentContainer, getSite(), toolkit);
        new RulesSection((IPolicyCmptType)getIpsObject(), parentContainer, getSite(), toolkit);
    }

    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {
        // Nothing to do.
    }

}
