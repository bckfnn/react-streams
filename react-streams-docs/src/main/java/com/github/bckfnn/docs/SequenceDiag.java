package com.github.bckfnn.docs;

import java.io.FileOutputStream;
import java.io.IOException;

import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

public class SequenceDiag {
    public Actor[] actors;
    public Call[] calls;

    public SequenceDiag() {
        Actor in = new Actor("input");
        Actor op = new Actor("LastOp");
        Actor out = new Actor("output");
        
        actors = new Actor[] {
                in,
                op,
                out,
        };
        
        calls = new Call[] {
                new Call(out, op, "request(n)"),
                new Call(op, in, "request(n)"),
                new Call(in, op, "onNext(T1)"),
                new Call(in, op, "onNext(T2)"),
                new Call(in, op, "onNext(T3)"),
                new Call(in, op, "onComplete()"),
                new Call(op, out, "onNext(T3)"),
                new Call(op, out, "onComplete()"),
        };
        
        generate();
    }
    
    public void generate() {
        int x = 50;
        for (Actor a : actors) {
            a.x = x;
            x += 70;
        }

        int y = 70;
        for (Call c : calls) {
            c.y = y;
            y += 20;
        }
        
        try {
            ParserContext context = new ParserContext();
            CompiledTemplate templ = TemplateCompiler.compileTemplate(getClass().getResourceAsStream("/sequenceDiag.mvel"), context);
            FileOutputStream out = new FileOutputStream("target/out.svg");
            TemplateRuntime.execute(templ, this, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        new SequenceDiag();
    }
    
    public static class Actor {
        public String name;
        public int x;

        public Actor(String name) {
            this.name = name;
        }
    }
    
    public static class Call {
        public Actor from;
        public Actor to;
        public String name;
        public int y;
        
        public Call(Actor from, Actor to, String name) {
            this.from = from;
            this.to = to;
            this.name = name;
        }
    }
    
}
