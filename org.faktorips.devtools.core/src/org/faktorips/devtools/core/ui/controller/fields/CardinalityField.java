/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Textfield to represent and edit cardinality values (which means int-values
 * and the asterisk (*)). The askerisk is mapped to Integer.MAX_VALUE on object
 * conversions and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class CardinalityField extends AbstractCardinalityField {
	private Text text;

	/**
	 * {@inheritDoc}
	 */
	public CardinalityField(Text text) {
		super();
		this.text = text;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void addListenerToControl() {
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(CardinalityField.this));
            }
            
        });
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText() {
		return text.getText();
	}

	/**
	 * {@inheritDoc}
	 */
	void setTextInternal(String newText) {
		text.setText(newText);
	}

	/**
	 * {@inheritDoc}
	 */
	public void insertText(String text) {
		this.text.insert(text);
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectAll() {
		text.selectAll();
	}	
}
