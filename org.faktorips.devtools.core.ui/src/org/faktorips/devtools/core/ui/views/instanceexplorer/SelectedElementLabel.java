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
package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of CLabel. Uses the data object and a <code>LabelProvider</code> to set the text and image of this label.
 * @author Cornelius Dirmeier
 *
 */
public class SelectedElementLabel extends CLabel {

	private ILabelProvider labelProvider;
	
	/**
	 * Constructor inherited from the CLabel component
	 * @param parent 
	 * @param style 
	 * 
	 */
	public SelectedElementLabel(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * To get the label provider of this label
	 * @return the associated label provider
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Setting a label provider 
	 * @param labelProvider
	 */
	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}
	
	@Override
	public void setData(Object data) {
		super.setData(data);
		if (data == null) {
			setText(null);
			setImage(null);
		} else {
			setText(labelProvider.getText(data));
			setImage(labelProvider.getImage(data));
		}
	}
	
	/**
	 * To refresh the label;
	 */
	public void refresh() {
		setData(getData());
		pack();
	}
	
	@Override
	public void dispose() {
		setData(null);
		super.dispose();
	}

}
