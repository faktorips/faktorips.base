package org.faktorips.devtools.core.ui.controller;

/**
 * @author eidenschink
 *
 * This class will replace the subclasses IpsObjectUIController and IpsPartUIController
 * when the implementation of the corresponding class IpsObjectPartContainer is finished.
 */
public abstract class IpsObjectPartContainerUIController extends DefaultUIController {


	public IpsObjectPartContainerUIController() {
		super();
	}

		public abstract void add(EditField editField, String propertyName);

}
