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

import org.faktorips.devtools.core.ui.editors.type.MethodsSection;
import org.faktorips.devtools.core.ui.editors.type.TypeEditorPage;

abstract class PolicyCmptTypeEditorPage extends TypeEditorPage {

    AttributesSection attributesSection;

    MethodsSection methodsSection;

    public PolicyCmptTypeEditorPage(PolicyCmptTypeEditor editor, boolean twoSectionsWhenTrueOtherwiseFour,
            String title, String pageId) {
        super(editor, twoSectionsWhenTrueOtherwiseFour, title, pageId);
    }

}
