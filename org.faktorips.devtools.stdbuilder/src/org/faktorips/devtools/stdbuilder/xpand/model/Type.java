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

package org.faktorips.devtools.stdbuilder.xpand.model;

public class Type {

    private final String qualifiedName;

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Type other = (Type)obj;
        if (qualifiedName == null) {
            if (other.qualifiedName != null) {
                return false;
            }
        } else if (!qualifiedName.equals(other.qualifiedName)) {
            return false;
        }
        return true;
    }

    public Type(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public Type(Class<?> clazz) {
        this.qualifiedName = clazz.getName();
    }

    /**
     * @return Returns the qualifiedName.
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

}
