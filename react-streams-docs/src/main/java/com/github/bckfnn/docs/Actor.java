package com.github.bckfnn.docs;

public class Actor {
    public Diagram diagram;
    public String name;
    public int x;
    public int stack = 0;

    public Actor(Diagram diagram, String name, int x) {
        this.diagram = diagram;
        this.name = name;
        this.x = x;
    }
    
    public Actor call(String methodName, Actor to) {
        return diagram.call(this, methodName, to);
    }
    
    public Actor returnTo(Actor to) {
        return diagram.returnTo(this, to);
    }

    public Actor returnTo(Actor to, int height) {
        return diagram.returnTo(this, to, height);
    }

    public String template() {
        return "src/main/resources/actor.mvel";
    }
}