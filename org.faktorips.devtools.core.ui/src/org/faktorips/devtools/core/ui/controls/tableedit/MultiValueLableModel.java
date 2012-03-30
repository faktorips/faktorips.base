/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.tableedit;

import java.util.List;

import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;

/**
 * Table model for a multi-value attribute. Manages the list of values the attribute contains.
 * <p>
 * Initially the model contains the values in the given {@link IAttributeValue}. They may be changed
 * by user interaction however. To "commit" the changed list of values {@link #applyValueList()}
 * must be called.
 * 
 * @author Stefan Widmaier
 */
public class MultiValueLableModel extends AbstractListTableModel<SingleValueHolder> {

    private final IAttributeValue attributeValue;

    public MultiValueLableModel(IAttributeValue attributeValue) {
        super(getValueList(attributeValue));
        this.attributeValue = attributeValue;
    }

    private static List<SingleValueHolder> getValueList(IAttributeValue attributeValue) {
        return getMultiValueHolder(attributeValue).getValue();
    }

    protected static MultiValueHolder getMultiValueHolder(IAttributeValue attributeValue) {
        return (MultiValueHolder)attributeValue.getValueHolder();
    }

    @Override
    public Object addElement() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue);
        getList().add(holder);
        return holder;
    }

    @Override
    public void removeElement(int index) {
        getList().remove(index);
    }

    /**
     * Applies the current list this model holds to the attribute's multi-value holder.
     */
    public void applyValueList() {
        getMultiValueHolder().setValue(getList());
    }

    protected MultiValueHolder getMultiValueHolder() {
        return getMultiValueHolder(attributeValue);
    }

}
