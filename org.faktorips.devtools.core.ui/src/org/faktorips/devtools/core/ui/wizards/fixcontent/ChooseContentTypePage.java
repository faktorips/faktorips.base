/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.fixcontent;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;

public class ChooseContentTypePage<T extends IIpsObject, E extends ILabeledElement> extends WizardPage {

    private DeltaFixWizardStrategy<T, E> contentStrategy;
    private AssignContentAttributesPage<T, E> assignContentAttributesPage;
    private UIToolkit uiToolkit;
    private IIpsObject content;
    private IIpsObject contentType;

    /** Creates the {@link ChooseContentTypePage}. */
    public ChooseContentTypePage(IIpsObject contentType, UIToolkit uiToolkit,
            DeltaFixWizardStrategy<T, E> contentStrategy,
            AssignContentAttributesPage<T, E> assignContentAttributesPage) {
        super(NLS.bind(Messages.FixContentWizard_chooseContentTypePageTitle, contentStrategy.getContentTypeString()));
        setTitle(
                NLS.bind(Messages.FixContentWizard_chooseContentTypePageTitle, contentStrategy.getContentTypeString()));
        setPageComplete(false);
        this.contentType = contentType;
        this.uiToolkit = uiToolkit;
        this.contentStrategy = contentStrategy;
        this.content = contentStrategy.getContent();
        this.assignContentAttributesPage = assignContentAttributesPage;
    }

    @Override
    public void createControl(Composite parent) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Choose ContentType control.
        uiToolkit.createFormLabel(workArea,
                NLS.bind(Messages.FixContentWizard_labelNewContentType, contentStrategy.getContentTypeString()));
        final IpsObjectRefControl contentTypeRefControl = contentStrategy.createContentTypeRefControl(uiToolkit,
                workArea);
        contentTypeRefControl.getTextControl().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                contentTypeModified(contentTypeRefControl);
            }
        });

        T newContentType = contentStrategy.findContentType(contentStrategy.getIpsProject());

        contentStrategy.createControl(newContentType, contentTypeRefControl);

        String message = NLS.bind(Messages.FixContentWizard_msgChooseContentType,
                new String[] { contentStrategy.getContentTypeString(), content.getUnqualifiedName() });
        setMessage(message);
        setControl(workArea);
    }

    /**
     * Update the wizards message area and check for page complete when the field to choose an
     * {@code ContentType} has been modified. Not used for {@link ITableContents}.
     */
    private void contentTypeModified(IpsObjectRefControl contentTypeRefControl) {
        String contentTypeString = contentStrategy.getContentTypeString();
        this.setMessage(NLS.bind(Messages.FixContentWizard_msgChooseContentType,
                new String[] { contentTypeString, content.getUnqualifiedName() }));

        String text = contentTypeRefControl.getText();

        contentType = contentStrategy.findContentType(contentStrategy.getIpsProject());

        this.setErrorMessage(null);
        boolean pageComplete = true;
        if (text.length() == 0) {
            this.setErrorMessage(NLS.bind(Messages.FixContentWizard_chosenContentTypeEmpty, contentTypeString));
            pageComplete = false;
        } else {
            if (contentType == null) {
                this.setErrorMessage(
                        NLS.bind(Messages.FixContentWizard_chosenContentTypeDoesNotExist, contentTypeString));
                pageComplete = false;
            } else if (contentType instanceof IEnumType) {
                pageComplete = enumTypeModified((IEnumType)contentType, pageComplete);
            }
        }

        this.setPageComplete(pageComplete);
        if (pageComplete) {
            assignContentAttributesPage.refreshControl();
        }
    }

    private boolean enumTypeModified(IEnumType newEnumType, boolean pageComplete) {
        boolean pageIsComplete = pageComplete;
        if (newEnumType.isAbstract()) {
            this.setErrorMessage(Messages.FixEnumContentWizard_chosenEnumTypeAbstract);
            pageIsComplete = false;
        }
        if (newEnumType.isInextensibleEnum()) {
            this.setErrorMessage(Messages.FixEnumContentWizard_chosenEnumTypeValuesArePartOfModel);
            pageIsComplete = false;
        }
        return pageIsComplete;
    }

}
