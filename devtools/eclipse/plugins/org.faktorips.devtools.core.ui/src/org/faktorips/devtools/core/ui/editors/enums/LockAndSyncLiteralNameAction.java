/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used by the <code>EnumValuesSection</code>. It enables the user to lock the values
 * of the literal name column and synchronize it's values with the default provider column.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class LockAndSyncLiteralNameAction extends Action {

    /** The name of the image for the action. */
    private static final String IMAGE_NAME = "SyncLiteralNames.gif"; //$NON-NLS-1$

    /** The <code>EnumValuesSection</code> this action belongs to. */
    private EnumValuesSection enumValuesSection;

    /**
     * Creates the <code>LockAndSyncLiteralNameAction</code>.
     * 
     * @param enumValuesSection The <code>EnumValuesSection</code> this action belongs to.
     * 
     * @throws NullPointerException If <code>enumValuesSection</code> is <code>null</code>.
     */
    public LockAndSyncLiteralNameAction(EnumValuesSection enumValuesSection) {
        super(Messages.EnumValuesSection_labelLockAndSync, IAction.AS_CHECK_BOX);
        ArgumentCheck.notNull(enumValuesSection);

        this.enumValuesSection = enumValuesSection;

        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_NAME));
        setToolTipText(Messages.EnumValuesSection_tooltipLockAndSync);
    }

    @Override
    public void run() {
        /*
         * We do not check here for isLockAndSyncLiteralNamesPossible() because the action won't be
         * enabled and therefore can't be executed if this is not the case.
         */
        enumValuesSection.toggleLockAndSyncLiteralNames();
    }

}
