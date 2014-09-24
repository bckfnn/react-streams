package com.github.bckfnn.docs;


public abstract class Invoke {
    public Actor from;
    public Actor to;
    public int y;
    public int height = 20;
    
    public int height() {
        return height;
    };
    
    public boolean isMethod() {
        return false;
    }
    
    abstract void exec();
}
