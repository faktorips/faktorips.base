package org.faktorips.devtools.core.ui.controller.fields;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.util.ArgumentCheck;

/**
 * Textfield to represent and edit cardinality values (which means int-values
 * and the asterisk (*)). The askerisk is mapped to Integer.MAX_VALUE on object
 * conversions and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class CardinalityField extends DefaultEditField {
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
	public Object getValue() {
		String text = getText();
        
		if (StringUtils.isEmpty(text)) {
            throw new RuntimeException("Can't return an Integer, field is empty."); //$NON-NLS-1$
        }

        Integer retValue = null;
		if (text.equals("*")) { //$NON-NLS-1$
			retValue = new Integer(Integer.MAX_VALUE);
		}
		else {
			retValue = Integer.valueOf(text);
		}

		return retValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(Object newValue) {
        ArgumentCheck.isInstanceOf(newValue, Integer.class);
		if (newValue instanceof Integer) {
			Integer value = (Integer)newValue;
			if (value.intValue() == Integer.MAX_VALUE) {
				setText("*"); //$NON-NLS-1$
			}
			else {
				setText(value.toString());
			}
		}
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
	public void setText(String newText) {
		try {
			Integer value = Integer.valueOf(newText);
			if (value.intValue() == Integer.MAX_VALUE) {
				text.setText("*"); //$NON-NLS-1$
			}
			else {
				text.setText(value.toString());
			}
		} catch (NumberFormatException e) {
			text.setText(newText);
		}		
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
