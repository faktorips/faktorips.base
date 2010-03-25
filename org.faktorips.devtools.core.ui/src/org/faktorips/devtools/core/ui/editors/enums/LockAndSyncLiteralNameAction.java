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

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used by the <tt>EnumValuesSection</tt>. It enables the user to lock the values of
 * the literal name column and synchronize it's values with the default provider column.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class LockAndSyncLiteralNameAction extends Action {

    /** The name of the image for the action. */
    private final String IMAGE_NAME = "SyncLiteralNames.gif";

    /** The <tt>EnumValuesSection</tt> this action belongs to. */
    private EnumValuesSection enumValuesSection;

    /**
     * Creates the <tt>LockAndSyncLiteralNameAction</tt>.
     * 
     * @param enumValuesSection The <tt>EnumValuesSection</tt> this action belongs to.
     * 
     * @throws NullPointerException If <tt>enumValuesSection</tt> is <tt>null</tt>.
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
