package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 * Interface for listeners which want to be notified before an ips object part ist deleted.
 * After all listeners are notified, the deletion will took place, no veto is supported.
 * 
 * @author Thorsten Guenther
 */
public interface IDeleteListener {

	/**
	 * Called before the method <code>IIpsObjectPart.delete()</code> is called.
	 * 
	 * @param part The part that will be deleted.
	 */
	public void aboutToDelete(IIpsObjectPart part);
}
