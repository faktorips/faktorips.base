/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.StringUtil;

/**
 * Defines the cardinality of a link. The cardinality consists of a minimum, a maximum and a default
 * value.
 * 
 * This class is immutable. If you like to "change" only one property you could call the with...
 * methods to get a new {@link Cardinality} with the one updated value.
 */
public class Cardinality implements Comparable<Cardinality> {

    public static final int CARDINALITY_MANY = IAssociation.CARDINALITY_MANY;

    public static final Cardinality UNDEFINED = new UndefinedCardinality();

    /**
     * Validation message code to indicate that the default cardinality is less than the min- or
     * greater than the max cardinality.
     */
    public static final String MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE = IProductCmptLink.MSGCODE_PREFIX
            + "DefaultCardinalityOutOfRange"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than the minimum
     * cardinality.
     */
    public static final String MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN = IProductCmptLink.MSGCODE_PREFIX
            + "MaxCardinalityIsLessThanMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than 1.
     */
    public static final String MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1 = IProductCmptLink.MSGCODE_PREFIX
            + "MaxCardinalityIsLessThan1"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than 1.
     */
    public static final String MSGCODE_MIN_CARDINALITY_IS_LESS_THAN_0 = IProductCmptLink.MSGCODE_PREFIX
            + "MinCardinalityIsLessThan0"; //$NON-NLS-1$

    private final int min;

    private final int max;

    private final int defaultCard;

    /**
     * Creates a new {@link Cardinality} with the specified properties
     * 
     * @param min The minimum cardinality
     * @param max The maximum cardinality
     * @param defaultCard The default cardinality
     */
    public Cardinality(int min, int max, int defaultCard) {
        this.min = min;
        this.max = max;
        this.defaultCard = defaultCard;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean isToMany() {
        return max == CARDINALITY_MANY;
    }

    public int getDefault() {
        return defaultCard;
    }

    /**
     * Creates a new Cardinality with the minimum cardinality updated to the specified value.
     * 
     * @param newMin The new minimum
     * @return A new cardinality with the same maximum and default but updated minimum
     */
    public Cardinality withMin(int newMin) {
        return new Cardinality(newMin, max, defaultCard);
    }

    /**
     * Creates a new Cardinality with the maximum cardinality updated to the specified value.
     * 
     * @param newMax The new maximum
     * @return A new cardinality with the same minimum and default but updated maximum
     */
    public Cardinality withMax(int newMax) {
        return new Cardinality(min, newMax, defaultCard);
    }

    /**
     * Creates a new Cardinality with the default cardinality updated to the specified value
     * 
     * @param newDefault The new default
     * @return A new cardinality with the same minimum and maximum but updated default
     */
    public Cardinality withDefault(int newDefault) {
        return new Cardinality(min, max, newDefault);
    }

    public MessageList validate(IProductCmptLink link) {
        MessageList result = new MessageList();
        if (min < 0) {
            String text = Messages.ProductCmptRelation_msgMinCardinalityIsLessThan0;
            result.add(new Message(MSGCODE_MIN_CARDINALITY_IS_LESS_THAN_0, text, Message.ERROR, link,
                    IProductCmptLink.PROPERTY_MIN_CARDINALITY));
        } else if (max < 1) {
            String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThan1;
            result.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1, text, Message.ERROR, link,
                    IProductCmptLink.PROPERTY_MAX_CARDINALITY));
        } else if (min > max) {
            String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThanMin;
            result.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN, text, Message.ERROR, link,
                    IProductCmptLink.PROPERTY_MIN_CARDINALITY, IProductCmptLink.PROPERTY_MAX_CARDINALITY));
        } else if (defaultCard > max || min > defaultCard || defaultCard == Integer.MAX_VALUE) {
            String text = NLS.bind(Messages.ProductCmptLink_msgDefaultCardinalityOutOfRange, Integer.toString(min),
                    isToMany() ? "*" : Integer.toString(max) //$NON-NLS-1$
            );
            result.add(new Message(MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE, text, Message.ERROR, link,
                    IProductCmptLink.PROPERTY_DEFAULT_CARDINALITY));
        }
        return result;
    }

    /** Returns the cardinality as a formatted string. */
    public String format() {
        return StringUtil.getRangeString(min, max, defaultCard);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + defaultCard;
        result = prime * result + max;
        result = prime * result + min;
        return result;
    }

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
        Cardinality other = (Cardinality)obj;
        if (defaultCard != other.defaultCard) {
            return false;
        }
        if (max != other.max) {
            return false;
        }
        if (min != other.min) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return format();
    }

    @Override
    public int compareTo(Cardinality o) {
        if (o == null || o instanceof UndefinedCardinality) {
            return -1;
        }

        if (this.min != o.min) {
            return Integer.compare(min, o.min);
        }

        if (max != o.max) {
            return Integer.compare(max, o.max);
        }

        return Integer.compare(defaultCard, o.defaultCard);
    }

    /**
     * Special cardinality to use for deleted {@link IProductCmptLink}. All values are 0, validation
     * is overridden to allow that.
     */
    private static class UndefinedCardinality extends Cardinality {

        private UndefinedCardinality() {
            super(0, 0, 0);
        }

        @Override
        public MessageList validate(IProductCmptLink link) {
            return new MessageList();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UndefinedCardinality;
        }

        @Override
        public int compareTo(Cardinality o) {
            if (o == null) {
                return -1;
            }
            if (o instanceof UndefinedCardinality) {
                return 0;
            }
            return 1;
        }
    }
}
