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

package org.faktorips.devtools.core.ui.bf.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.type.IMethod;

public class CallMethodDecisionPropertySection extends CallMethodPropertySection {

    private Label valueLabel;

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        update();
    }

    @Override
    protected void extendControlArea(Composite area) {
        uiToolkit.createLabel(area, Messages.CallMethodDecisionPropertySection_labelDatatype);
        valueLabel = uiToolkit.createLabel(area, ""); //$NON-NLS-1$
    }

    private void update() {
        IDecisionBFE decisionBFE = (IDecisionBFE)getBFElement();
        IMethod method;
        try {
            method = decisionBFE.findMethod(getBFElement().getIpsProject());
            if (method != null) {
                valueLabel.setText(method.getDatatype());
            } else {
                valueLabel.setText(""); //$NON-NLS-1$
            }
        } catch (CoreException e1) {
            IpsPlugin.log(e1);
        }
    }

    @Override
    protected void updateFromModel(ContentChangeEvent event) {
        update();
    }
}
