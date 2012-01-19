package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.internal.model.productcmpt.DeepCopyOperation;
import org.faktorips.devtools.core.internal.model.productcmpt.DeepCopyOperationFixup;

/**
 * When a plugin needs additional information for a {@link DeepCopyOperationFixup} it should
 * register an {@link AdditionalDeepCopyWizardPage} via the extension point
 * {@code org.faktorips.devtools.core.ui.DeepCopyWizard}. When the
 * {@link DeepCopyWizard#performFinish()} method runs, all {@link AdditionalDeepCopyWizardPage
 * additional pages} will be called to {@link AdditionalDeepCopyWizardPage#configureFixups(List)
 * configure} their {@link DeepCopyOperationFixup}, which then is called when the
 * {@link DeepCopyOperation} is run.
 * <p>
 * <strong>This feature is experimental and the interface may change in future releases.</strong>
 * </p>
 * 
 * @since 3.6
 */
public interface AdditionalDeepCopyWizardPage extends IWizardPage {
    String CONFIG_ELEMENT_ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
    String CONFIG_ELEMENT_ID_ADDITIONAL_PAGE = "additionalPage"; //$NON-NLS-1$
    String EXTENSION_POINT_ID_DEEP_COPY_WIZARD = "deepCopyWizard"; //$NON-NLS-1$

    /**
     * Provide the information gathered in this page to relevant {@link DeepCopyOperationFixup
     * DeepCopyOperationFixups}.
     * <p>
     * <strong>This method is experimental and the signature may change in future releases.</strong>
     * </p>
     * 
     * @param additionalFixups all {@link DeepCopyOperationFixup DeepCopyOperationFixups} that will
     *            be called by the {@link DeepCopyOperation}
     */
    void configureFixups(List<DeepCopyOperationFixup> additionalFixups);
}