package de.di.xml;

/**
 *
 * @author A. Sopicki
 */
public class IndexValue {

    private String value = null;
    
    private String attribute = null;
    
    private IndexValue next = null;
    
    private IndexValue sibling = null;
    
    public IndexValue(String attr, String v) {
        value = v;
        attribute = attr;
    }
    
    public String getAttribute() {
        return attribute;
    }
    
    public String getValue() {
        return value;
    }
    
    public IndexValue getNext() {
        return next;
    }
    
    public void setNext(IndexValue n) {
        next = n;
    }
    
    public IndexValue getSibling() {
        return sibling;
    }
    
    public void setSibling(IndexValue s) {
        sibling = s;
    }
    
    @Override
    public String toString(){
        return this.attribute + " : " + this.value;
    }
}
