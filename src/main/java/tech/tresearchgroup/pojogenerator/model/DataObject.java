package tech.tresearchgroup.pojogenerator.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataObject {
    private String className;
    private Map<String, List<Object>> attributes = new HashMap<>();

    public DataObject() {
    }

    public DataObject(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setAttributes(Map<String, List<Object>> attributes) {
        this.attributes = attributes;
    }

    public Map<String, List<Object>> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "DataObject{" +
            "className='" + className + '\'' +
            ", attributes=" + attributes +
            '}';
    }

    public void addAttribute(String name, Object objObj) {
        List list = attributes.get(name);
        if(list == null) {
            attributes.put(name, new LinkedList<>());
            attributes.get(name).add(objObj);
        } else {
            list.add(objObj);
        }
    }
}
