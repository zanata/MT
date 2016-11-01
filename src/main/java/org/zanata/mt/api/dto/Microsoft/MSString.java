package org.zanata.mt.api.dto.Microsoft;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSString implements Serializable {
    private String value;

    public MSString() {
        this(null);
    }

    public MSString(String value) {
        this.value = value;
    }

    @XmlValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MSString)) return false;

        MSString msString = (MSString) o;

        return value != null ? value.equals(msString.value) :
            msString.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
