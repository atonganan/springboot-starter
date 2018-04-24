package net.gradle.springboot.rest;

public class NestedChild extends Nested{
    public String property;

    public NestedChild(String name, int code,String property) {
        super(name, code);
        this.property = property;
    }

}
