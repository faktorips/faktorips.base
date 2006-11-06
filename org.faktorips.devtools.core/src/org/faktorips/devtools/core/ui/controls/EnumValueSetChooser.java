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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.util.message.MessageList;

/**
 * Control to select values from one enum value set to be added to another.
 * 
 * @author Thorsten Guenther
 */
public class EnumValueSetChooser extends ListChooser {

	/**
	 * The value set to modify 
	 */
	private IEnumValueSet target;

    /**
     * The value set to get the values from 
     */
    private IEnumValueSet source;

    /**
	 * The controller to notify of changes to the target value set
	 */
	private DefaultUIController uiController;
	
	/**
	 * Mapping of human representation names to value ids.
	 */
	private Map id2name = new HashMap();
	
	/**
     * @param parent The parent control
     * @param toolkit The toolkit to make creation of UI easier.
     * @param source The source-valueset. Can be <code>null</code> if no restriction is applied to
     *            the items of the enum datatype.
     * @param target The target-valueset (the one to add the values to).
     * @param type The EnumDatatype
     * @param uiController The controller to notify upon change
     */
    public EnumValueSetChooser(Composite parent, UIToolkit toolkit, IEnumValueSet source, IEnumValueSet target,
            EnumDatatype type, DefaultUIController uiController) {
        super(parent, toolkit);
        this.target = target;
        this.source = source;
        this.uiController = uiController;
        super.setTargetContent(getTargetValues(target, type));
        super.setSourceContent(getSourceValues(source, target, type));
    }

	/**
	 * {@inheritDoc}
	 */
	public void valuesAdded(String[] values) {
		for (int i = 0; i < values.length; i++) {
			target.addValue(getIdForName(values[i]));
		}
		uiController.updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	public void valuesRemoved(String[] values) {
		for (int i = 0; i < values.length; i++) {
			target.removeValue(getIdForName(values[i]));
		}
		uiController.updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	public void valueMoved(String value, int index, boolean up) {
		int newIndex;
		if (up) {
			newIndex = index - 1;
		} else {
			newIndex = index + 1;
		}
		String old = target.getValue(newIndex);
		target.setValue(newIndex, value);
		target.setValue(index, getIdForName(old));

	}	
	
	/**
	 * Returns the value id for the given human readable name.
	 */
	private String getIdForName(String name) {
		return (String)id2name.get(name);
	}

	/**
	 * Returns an array of human readable strings representing all values contained in the
	 * given valueset. The names are requested from the given datatype. If the type is <code>null</code>
	 * or does not suppert value names, the ids are returned as names.
	 * 
	 * @param valueSet The valueset to get the names for.
	 * @param type The datatype to get the names from. Can be <code>null</code>.
	 */
	private String[] getTargetValues(IEnumValueSet valueSet, EnumDatatype type) {
		String[] ids = valueSet.getValues();
		return mapIds2Names(ids, type);
	}
	
	/**
	 * Returns an array of human readable strings representing all values contained in 
	 * sourceSet but not in targetSet. The names are requested from the given datatype. If the 
	 * type is <code>null</code> or does not support value names, the ids are returned as names.
	 * 
	 * @param sourceSet The set to get the values from.
	 * @param targetSet All values in this set are excluded from the result.
	 * @param type The type to get the names from. Can be <code>null</code>.
	 * @return
	 */
	private String[] getSourceValues(IEnumValueSet sourceSet, IEnumValueSet targetSet, EnumDatatype type) {
		String[] ids = new String[0];
		if (sourceSet == null && type != null) {
			List targetIds = Arrays.asList(targetSet.getValues());
			String[] allIds = type.getAllValueIds(true);
			List result = new ArrayList();
			for (int i = 0; i < allIds.length; i++) {
				if (!targetIds.contains(allIds[i])) {
					result.add(allIds[i]);
				}
			}
			ids = (String[])result.toArray(new String[result.size()]);
			
		} else {
			ids = targetSet.getValuesNotContained(sourceSet);
		}
		return mapIds2Names(ids, type);
	}
	
	/**
	 * Maps all given ids to names returned by the given datatype. If the
	 * datatype is <code>null</code> or does not support value names, the
	 * ids are returned without name.
	 * 
	 * @param ids The ids to map to names.
	 * @param type The type to use to find the names.
	 * @return An array of human readable names. The names can be mapped back to ids
	 * using the method getIdForName().
	 */
	private String[] mapIds2Names(String[] ids, EnumDatatype type) {
		List result = new ArrayList();
		for (int i = 0; i < ids.length; i++) {
			String name;
			if (type != null && type.isSupportingNames()) {
				name = type.getValueName(ids[i]) + " (" + ids[i] + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				name = ids[i];
			}

			if (name == null) {
				name = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
			}
			id2name.put(name, ids[i]);
			result.add(name);
		}

		return (String[]) result.toArray(new String[result.size()]);
	}
    
    /**
     * {@inheritDoc}
     */
    public MessageList getMessagesFor(String value) {
        String id = (String)this.id2name.get(value);
        MessageList result = new MessageList();
        if(source == null){
            return result;
        }
        source.containsValue(id, result, target, null);
        return result;
    }
}
