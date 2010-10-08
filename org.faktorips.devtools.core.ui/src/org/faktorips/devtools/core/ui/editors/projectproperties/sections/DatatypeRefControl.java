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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DatatypeCompletionProcessor;
import org.faktorips.devtools.core.ui.DatatypeSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;

/**
 * A control that allows to edit a reference to a datatype.
 */
public class DatatypeRefControl extends TextButtonControl {

    private ArrayList<String> prefinedDatatype;

    private DatatypeCompletionProcessor completionProcessor;

    public DatatypeRefControl(ArrayList<String> prefinedDatatype, Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, Messages.PredefinedDataypeDialog_Label);
        this.prefinedDatatype = prefinedDatatype;

        completionProcessor = new DatatypeCompletionProcessor();
        // completionProcessor.setIpsProject(project);
        // CompletionUtil.createContentAssistant(completionProcessor);
        // ContentAssistHandler.createHandlerForText(text,
        // CompletionUtil.createContentAssistant(completionProcessor));
    }

    public void setVoidAllowed(boolean includeVoid) {
        completionProcessor.setIncludeVoid(includeVoid);
    }

    public void setPrimitivesAllowed(boolean includePrimitives) {
        completionProcessor.setIncludePrimitives(includePrimitives);
    }

    public boolean getPrimitivesAllowed() {
        return completionProcessor.isIncludePrimitives();
    }

    public boolean isVoidAllowed() {
        return completionProcessor.isIncludeVoid();
    }

    public void setOnlyValueDatatypesAllowed(boolean valuetypesOnly) {
        completionProcessor.setValueDatatypesOnly(valuetypesOnly);
    }

    public boolean isOnlyValueDatatypesAllowed() {
        return completionProcessor.getValueDatatypesOnly();
    }

    public void setAbstractAllowed(boolean abstractAllowed) {
        completionProcessor.setIncludeAbstract(abstractAllowed);
    }

    public boolean isAbstractAllowed() {
        return completionProcessor.isIncludeAbstract();
    }

    public void setDisallowedDatatypes(List<Datatype> disallowedDatatypes) {
        completionProcessor.setExcludedDatatypes(disallowedDatatypes);
    }

    public List<Datatype> getDisallowedDatatypes() {
        return completionProcessor.getExcludedDatatypes();
    }

    @Override
    protected void buttonClicked() {
        try {
            DatatypeSelectionDialog dialog = new DatatypeSelectionDialog(getShell());
            dialog.setElements(prefinedDatatype.toArray());
            if (dialog.open() == Window.OK) {
                String textToSet = ""; //$NON-NLS-1$
                if (dialog.getResult().length > 0) {
                    String datatype = (String)dialog.getResult()[0];
                    textToSet = datatype;
                }
                try {
                    immediatelyNotifyListener = true;
                    text.setText(textToSet);
                } finally {
                    immediatelyNotifyListener = false;
                }
            }
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

}
