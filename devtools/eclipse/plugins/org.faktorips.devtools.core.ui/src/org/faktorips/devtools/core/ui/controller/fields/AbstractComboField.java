/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract Field that handles a {@link Combo} component.
 * 
 * @see EditField for details about generic type T
 */
public abstract class AbstractComboField<T> extends DefaultEditField<T> {

    private Combo combo;
    private boolean immediatelyNotifyListener = false;

    public AbstractComboField() {
        super();
    }

    public AbstractComboField(Combo combo) {
        this();
        ArgumentCheck.notNull(combo);
        this.combo = combo;
    }

    @Override
    public Control getControl() {
        return combo;
    }

    /**
     * Returns the text control this is an edit field for.
     */
    public Combo getComboControl() {
        return combo;
    }

    @Override
    public String getText() {
        return combo.getText();
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            if (newText == null) {
                combo.setText(getNullStringRepresentation());
            } else {
                combo.setText(newText);
            }
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    private String getNullStringRepresentation() {
        if (supportsNullStringRepresentation()) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public void insertText(String insertText) {
        // cannot insert text into combo
    }

    @Override
    public void selectAll() {
        combo.setSelection(new Point(0, getText().length()));
    }

    @Override
    protected void addListenerToControl() {
        final ModifyListener modifyListener = $ -> notifyChangeListeners(
                new FieldValueChangedEvent(AbstractComboField.this), immediatelyNotifyListener);
        combo.addModifyListener(modifyListener);
        combo.addDisposeListener($ -> combo.removeModifyListener(modifyListener));
    }

}
