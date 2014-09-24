package com.github.bckfnn.docs;

public class Call extends Invoke {
    public String name;
    
    public Call(Actor from, Actor to, String name) {
        this.from = from;
        this.to = to;
        this.name = name;
    }

    public boolean isMethod() {
        return true;
    }
    
    public String template() {
        return "src/main/resources/call.mvel";
    }
    
    public void exec() {
        to.stack++;
    }

}
