package org.josso.gateway;

import java.io.Serializable;


/**
 * An util to represent name-value pairs.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSONameValuePair.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class SSONameValuePair implements Serializable {

    private String _name;
    private String _value;

    public SSONameValuePair(String name, String value) {
        _name = name;
        _value = value;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

}

