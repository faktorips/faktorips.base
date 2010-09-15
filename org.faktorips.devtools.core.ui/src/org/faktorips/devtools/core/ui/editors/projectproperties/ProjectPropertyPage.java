/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.projectproperties;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.Messages;

public class ProjectPropertyPage extends FormPage {
    /** The horizontal space between two sections. */
    public final static int HORIZONTAL_SECTION_SPACE = 15;

    /** The vertical space between two sections. */
    public final static int VERTICAL_SECTION_SPACE = 10;

    private UIToolkit uiToolkit;
    final static String PAGEID = "Datatypes";

    public ProjectPropertyPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        uiToolkit = new UIToolkit(new FormToolkit(Display.getCurrent()));
    }

    public ProjectPropertyPage(ProjectPropertyEditor editor) {
        super(editor, PAGEID, Messages.DescriptionPage_description);
    }

    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        // new LabelSection(null, formBody, toolkit);
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        if (getIpsObject() == null) {
            // No valid IPS source file, create nothing.
            return;
        }

        form.setText(getProjectPropertyEditor().getTitle());
        FormToolkit toolkit = managedForm.getToolkit();
        form.setExpandHorizontal(true);
        form.setExpandVertical(true);
        form.reflow(true);
        createPageContent(form.getBody(), new UIToolkit(toolkit));
        // registerSelectionProviderActivation(getPartControl());
    }

    /** Returns the <tt>IpsObjectEditor</tt> this page belongs to. */
    public ProjectPropertyEditor getProjectPropertyEditor() {
        return (ProjectPropertyEditor)getEditor();
    }

    public IIpsProjectProperties getIpsObject() {
        /*
         * Null checking is necessary since it might be the case that the IPS source file cannot be
         * determined. E.g. in the special case that one tries to open an IPS source file which is
         * not in an IPS package.
         */
        if (getProjectPropertyEditor() != null && getProjectPropertyEditor().getProperty() != null) {
            return getProjectPropertyEditor().getProperty();
        } else {
            return null;
        }
    }

    protected GridLayout createPageLayout(int numOfColumns, boolean equalSize) {
        GridLayout layout = new GridLayout(numOfColumns, equalSize);
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        layout.verticalSpacing = VERTICAL_SECTION_SPACE;
        layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;

        return layout;
    }

    // private static class LabelSection extends IpsSection {
    //
    // private final ILabeledElement labeledElement;
    //
    // private LabelEditComposite labelEditComposite;
    //
    // private LabelSection(ILabeledElement labeledElement, Composite parent, UIToolkit toolkit) {
    // super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
    //
    // this.labeledElement = labeledElement;
    //
    // initControls();
    // setText(Messages.LabelSection_label);
    // }
    //
    // @Override
    // protected void initClientComposite(Composite client, UIToolkit toolkit) {
    // labelEditComposite = new LabelEditComposite(client, labeledElement);
    // }
    //
    // @Override
    // protected void performRefresh() {
    // if (labeledElement == null) {
    // return;
    // }
    // labelEditComposite.refresh();
    // }
    //
    // }
}
