/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.tablecontents.ITableContents;

public class ChooseContentTypePage<T extends IIpsObject, E extends ILabeledElement> extends WizardPage {

    private DeltaFixWizardStrategy<T, E> contentStrategy;
    private AssignContentAttributesPage<T, E> assignContentAttributesPage;
    private UIToolkit uiToolkit;
    private IIpsObject content;

    /** Creates the {@link ChooseContentTypePage}. */
    public ChooseContentTypePage(UIToolkit uiToolkit,
            DeltaFixWizardStrategy<T, E> contentStrategy,
            AssignContentAttributesPage<T, E> assignContentAttributesPage) {
        super(NLS.bind(Messages.FixContentWizard_chooseContentTypePageTitle, contentStrategy.getContentTypeString()));
        setTitle(
                NLS.bind(Messages.FixContentWizard_chooseContentTypePageTitle, contentStrategy.getContentTypeString()));
        setPageComplete(false);
        this.uiToolkit = uiToolkit;
        this.contentStrategy = contentStrategy;
        content = contentStrategy.getContent();
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
        contentTypeRefControl.getTextControl().addModifyListener($ -> contentTypeModified(contentTypeRefControl));

        T newContentType = contentStrategy.findContentType(contentStrategy.getIpsProject(), null);

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

        T contentType = contentStrategy.findContentType(contentStrategy.getIpsProject(), text);

        setErrorMessage(null);
        boolean pageComplete = true;
        if (text.length() == 0) {
            setErrorMessage(NLS.bind(Messages.FixContentWizard_chosenContentTypeEmpty, contentTypeString));
            pageComplete = false;
        } else {
            if (contentType == null) {
                setErrorMessage(
                        NLS.bind(Messages.FixContentWizard_chosenContentTypeDoesNotExist, contentTypeString));
                pageComplete = false;
            } else if (contentType instanceof IEnumType) {
                pageComplete = enumTypeModified((IEnumType)contentType, pageComplete);
            }
        }

        setPageComplete(pageComplete);
        if (pageComplete) {
            assignContentAttributesPage.refreshControl();
        }
    }

    private boolean enumTypeModified(IEnumType newEnumType, boolean pageComplete) {
        boolean pageIsComplete = pageComplete;
        if (newEnumType.isAbstract()) {
            setErrorMessage(Messages.FixEnumContentWizard_chosenEnumTypeAbstract);
            pageIsComplete = false;
        }
        if (newEnumType.isInextensibleEnum()) {
            setErrorMessage(Messages.FixEnumContentWizard_chosenEnumTypeValuesArePartOfModel);
            pageIsComplete = false;
        }
        return pageIsComplete;
    }

}
