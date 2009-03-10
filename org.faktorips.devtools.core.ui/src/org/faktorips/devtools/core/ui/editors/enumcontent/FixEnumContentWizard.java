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

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IMessage;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.EnumTypeRefControl;

/**
 * This wizard is available trough the <code>EnumContentEditor</code> when the
 * <code>IEnumContent</code> to edit does not refer to a valid <code>IEnumType</code> or the number
 * of referenced enum attributes that is stored in the <code>IEnumContent</code> does not correspond
 * to the number of enum attributes defined in the referenced <code>IEnumType</code>.
 * <p>
 * On the first page the wizard lets the user select a valid <code>IEnumType</code> to refer to. The
 * second page provides comfortable assignment of existing enum attribute values to enum attributes.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class FixEnumContentWizard extends Wizard {

    /** The image for this wizard. */
    private final String IMAGE = "wizards/BrokenEnumWizard.png";

    /** The <code>IEnumContent</code> to set a new <code>IEnumType</code> for. */
    private IEnumContent enumContent;

    /** The ui toolkit to create new ui elements with. */
    private UIToolkit uiToolkit;

    /**
     * Creates a new <code>FixEnumContentWizard</code>.
     * 
     * @param enumContent The <code>IEnumContent</code> to fix.
     */
    public FixEnumContentWizard(IEnumContent enumContent) {
        this.enumContent = enumContent;
        this.uiToolkit = new UIToolkit(null);

        setWindowTitle(Messages.FixEnumContentWizard_title);
        setNeedsProgressMonitor(false);
        setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        addPage(new ChooseEnumTypePage());
        // TODO aw: assignEnumAttributesPage
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        // TODO aw: performFinish()
        return false;
    }

    /** The wizard page that lets the user choose a new enum type. */
    private class ChooseEnumTypePage extends WizardPage {

        /**
         * Creates a new <code>ChooseEnumTypePage</code>.
         */
        private ChooseEnumTypePage() {
            super(Messages.FixEnumContentWizard_chooseEnumTypePageTitle);
        }

        /**
         * {@inheritDoc}
         */
        public void createControl(Composite parent) {
            Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
            workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

            // Choose enum type control
            uiToolkit.createFormLabel(workArea, Messages.FixEnumContentWizard_labelNewEnumType);
            final EnumTypeRefControl enumTypeRefControl = uiToolkit.createEnumTypeRefControl(enumContent
                    .getIpsProject(), workArea, false);
            enumTypeRefControl.getTextControl().addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent event) {
                    try {

                        String text = enumTypeRefControl.getText();
                        IEnumType newEnumType = enumContent.getIpsProject().findEnumType(text);

                        boolean pageComplete = false;
                        if (newEnumType != null) {
                            if (!(newEnumType.isAbstract()) && !(newEnumType.getValuesArePartOfModel())) {
                                pageComplete = true;
                            }
                            if (newEnumType.isAbstract()) {
                                setMessage("ABSTRACT", IMessage.ERROR); // TODO aw
                            }
                            if (newEnumType.getValuesArePartOfModel()) {
                                setMessage("VALUES PART OF MODEL", IMessage.ERROR); // TODO aw
                            }
                        } else {
                            setMessage("NOT EXIST", IMessage.ERROR); // TODO aw
                        }

                        setPageComplete(pageComplete);

                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            try {
                IEnumType enumType = enumContent.findEnumType();
                if (enumType != null) {
                    if (!(enumType.isAbstract()) && !(enumType.getValuesArePartOfModel())) {
                        enumTypeRefControl.setText(enumType.getQualifiedName());
                    }
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            setMessage(Messages.FixEnumContentWizard_msgChooseEnumType);
            setControl(workArea);
        }

    }

}
