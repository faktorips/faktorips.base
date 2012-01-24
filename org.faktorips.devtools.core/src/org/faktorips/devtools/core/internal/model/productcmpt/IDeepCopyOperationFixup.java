package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * A {@link IDeepCopyOperationFixup} will be run at the end of the
 * {@link DeepCopyOperation#run(IProgressMonitor)} method to allow additional changes to the
 * copied objects by plugins.
 * <p>
 * <strong>This feature is experimental and the interface may change in future
 * releases.</strong>
 * </p>
 * 
 * @since 3.6
 */
public interface IDeepCopyOperationFixup {
    String CONFIG_ELEMENT_ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
    String CONFIG_ELEMENT_ID_FIXUP = "Fixup"; //$NON-NLS-1$
    String EXTENSION_POINT_ID_DEEP_COPY_OPERATION = "deepCopyOperation"; //$NON-NLS-1$

    /**
     * Makes changes to the new {@link IProductCmpt} after the {@link DeepCopyOperation}. The
     * old {@link IProductCmpt} is passed as reference.
     * <p>
     * <strong>This method is experimental and the signature may change in future
     * releases.</strong>
     * </p>
     * 
     * @param productCmptNew the new {@link IProductCmpt}
     * @param productCmptTemplate the old {@link IProductCmpt}
     */
    void fix(IProductCmpt productCmptNew, IProductCmpt productCmptTemplate);
}