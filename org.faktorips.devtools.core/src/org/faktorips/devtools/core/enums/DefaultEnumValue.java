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

package org.faktorips.devtools.core.enums;

/**
 * Default implementation of <tt>EnumValue</tt>.
 * 
 * @see EnumValue
 */
public class DefaultEnumValue implements EnumValue {

    private DefaultEnumType type;

    private String id;

    private String name;

    public DefaultEnumValue(DefaultEnumType type, String id) {
        this(type, id, id);
    }

    public DefaultEnumValue(DefaultEnumType type, String id, String name) {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.id = id;
        this.name = name;
        type.addValue(this);
    }

    @Override
    public EnumType getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EnumValue)) {
            return false;
        }
        EnumValue other = (EnumValue)o;
        return id.equals(other.getId()) && type.equals(other.getType());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return type.toString() + '.' + id;
    }

    @Override
    public int compareTo(Object o) {
        EnumValue other = (EnumValue)o;
        return id.compareTo(other.getId());
    }

}
