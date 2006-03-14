/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Control which displays two lists, seperated by four buttons. The left list
 * contains the source values, the right one the target values. The buttons between
 * the both lists allow to take values from one list to the other. 
 * <p>
 * At the right of the target list, two buttons allow to modify the sort order
 * of the items in this list.
 * 
 * @author Thorsten Guenther
 */
public abstract class ListChooser extends Composite {

	private UIToolkit toolkit;
	private List source;
	private List target;
	private Button addSelected;
	private Button addAll;
	private Button removeSelected;
	private Button removeAll;
	private Button up;
	private Button down;
	
	/**
	 * Creates a new list chooser.
	 *  
	 * @param parent The parent control.
	 * @param toolkit Toolkit to easily create the UI.
	 * @param sourceContent All values which should show up in the source list.
	 * @param targetContent All values which should show up in the target list.
	 */
	public ListChooser(Composite parent, UIToolkit toolkit) {
		super(parent, SWT.NONE);
		this.toolkit = toolkit;
		
		this.setLayout(new GridLayout(4, false));
		
		toolkit.createLabel(this, Messages.ListChooser_labelAvailableValues);
		toolkit.createLabel(this, ""); //$NON-NLS-1$
		toolkit.createLabel(this, Messages.ListChooser_lableChoosenValues);
		toolkit.createLabel(this, ""); //$NON-NLS-1$
		
		source = new List(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		source.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		addChooseButtons();
		target = new List(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		target.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		addMoveButtons();

		addSelected.addSelectionListener(new ChooseListener(source, target, false));
		removeSelected.addSelectionListener(new ChooseListener(target, source, false));
		addAll.addSelectionListener(new ChooseListener(source, target, true));
		removeAll.addSelectionListener(new ChooseListener(target, source, true));
	}

	/**
	 * Set the content of the source-list.
	 */
	protected void setSourceContent(String[] srcContent) {
		source.setItems(srcContent);
	}

	/**
	 * Set the content of the target-list.
	 */
	protected void setTargetContent(String[] targetContent) {
		target.setItems(targetContent);
	}

	/**
	 * Returns all values contained in the target list. The order of the items in the array
	 * is the order the user has choosen.
	 */
	public String[] getTargetContent() {
		return target.getItems();
	}

	/**
	 * Add the buttons to take a value from left to right or vice versa.
	 */
	private void addChooseButtons() {
		Composite root = new Composite(this, SWT.NONE);
		root.setLayout(new GridLayout(1, false));
		root.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		
		addSelected = toolkit.createButton(root, ">"); //$NON-NLS-1$
		removeSelected = toolkit.createButton(root, "<"); //$NON-NLS-1$
		addAll = toolkit.createButton(root, ">>"); //$NON-NLS-1$
		removeAll = toolkit.createButton(root, "<<"); //$NON-NLS-1$

		addSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		removeSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		removeAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	/**
	 * Add the buttons to move a value in the target list up or down.
	 */
	private void addMoveButtons() {
		Composite root = new Composite(this, SWT.NONE);
		root.setLayout(new GridLayout(1, false));
		root.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		
		up = toolkit.createButton(root, Messages.ListChooser_buttonUp);
		down = toolkit.createButton(root, Messages.ListChooser_buttonDown);
		
		up.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		down.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		up.addSelectionListener(new MoveListener());
		down.addSelectionListener(new MoveListener());
	}

	/**
	 * This method is called when new values are added to the target list.
	 * @param values The new values.
	 */
	public abstract void valuesAdded(String[] values);
	
	/**
	 * This method is called when values are removed from the target list.
	 * @param values The removed values.
	 */
	public abstract void valuesRemoved(String[] values);
	
	/**
	 * This method is called, when the order of items has changed in the target list.
	 * @param value The value moved.
	 * @param index The index, the value is located.
	 * @param up <code>true</code> if the value is moved upwards, <code>false</code> otherwise.
	 */
	public abstract void valueMoved(String value, int index, boolean up);
	
	private void notify(List modified, String[] values, boolean removed) {
		if (modified == source) {
			return;
		}
		
		if (removed) {
			valuesRemoved(values);
		}
		else {
			valuesAdded(values);
		}
	}

	/**
	 * Listener to handle the modification of object-order in
	 * target list. 
	 * 
	 * @author Thorsten Guenther
	 */
	private class MoveListener implements SelectionListener {

		/**
		 * {@inheritDoc}
		 */
		public void widgetSelected(SelectionEvent e) {
			int[] selected = target.getSelectionIndices();
			
			if (selected.length == 0) {
				// nothing selected
				return;
			}
			
			Arrays.sort(selected);

			if (e.getSource() == up) {
				if (selected[0] == 0) {
					// allready at top
					return;
				}
				
				for (int i = 0; i < selected.length; i++) {
					valueMoved(target.getItem(selected[i]), selected[i], true);
					String old = target.getItem(selected[i]-1);
					target.setItem(selected[i]-1, target.getItem(selected[i]));
					target.setItem(selected[i], old);
					selected[i]--;
				}
				target.setSelection(selected);
			} else {
				if (selected[selected.length-1] == target.getItemCount()-1) {
					// allready at bottom
					return;
				}

				for (int i = 0; i < selected.length; i++) {
					valueMoved(target.getItem(selected[i]), selected[i], false);
					String old = target.getItem(selected[i]+1);
					target.setItem(selected[i]+1, target.getItem(selected[i]));
					target.setItem(selected[i], old);
					selected[i]++;
				}
				target.setSelection(selected);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
		
	}
	
	/**
	 * Listener to handle the move of values from source to target and vice versa.
	 * 
	 * @author Thorsten Guenther
	 */
	private class ChooseListener implements SelectionListener {

		private List src;
		private List trgt;
		private boolean moveAll;
		
		/**
		 * Creates a new listener to handle modifcations at the given lists.
		 * 
		 * @param source The source where to remove the objects
		 * @param target The target where to put the objects
		 * @param all Flag to respect the selection (<code>false</code>) or take 
		 * all values (<code>true</code>).
		 */
		public ChooseListener(List source, List target, boolean all) {
			this.src = source;
			this.trgt = target;
			this.moveAll = all;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void widgetSelected(SelectionEvent e) {
			String[] toMove;
			if (moveAll) {
				toMove = src.getItems();
				src.removeAll();
				ListChooser.this.notify(src, toMove, true);
			}
			else {
				toMove = src.getSelection();
				src.remove(src.getSelectionIndices());
				ListChooser.this.notify(src, toMove, true);
			}
			
			for (int i = 0; i < toMove.length; i++) {
				trgt.add(toMove[i]);
			}
			ListChooser.this.notify(trgt, toMove, false);
		}

		/**
		 * {@inheritDoc}
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

	}

}
