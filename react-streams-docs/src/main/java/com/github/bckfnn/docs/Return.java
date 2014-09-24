package com.github.bckfnn.docs;


public class Return extends Invoke {
    public Return(Actor from, Actor to) {
        this(from, to, 10);
    }
    
    public Return(Actor from, Actor to, int height) {
        this.from = from;
        this.to = to;
        this.height = height;
    }
    
    public String template() {
        return "src/main/resources/return.mvel";
    }

    public void exec() {
        from.stack--;
    }

}
