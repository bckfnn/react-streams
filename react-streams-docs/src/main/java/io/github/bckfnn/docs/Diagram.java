package io.github.bckfnn.docs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;


public class Diagram {
    private String title;
    
    List<Actor> actors = new ArrayList<Actor>();
    List<Invoke> calls = new ArrayList<Invoke>();
    int x = 50;
    int y = 50;
    
    public Diagram(String title) {
        this.title = title;
    }
    
    public Actor actor(String name) {
        return actor(name, 70);
    }
    
    public Actor actor(String name, int width) {
        Actor actor = new Actor(this, name, x);
        actors.add(actor);
        x += width; 
        return actor;
    }
    
    public void addInvoke(Invoke invoke) {
        y += invoke.height();
        invoke.y = y;
        calls.add(invoke);
    }
    
    public Actor call(Actor from, String methodName, Actor to) {
        addInvoke(new Call(from, to, methodName));
        return to;
    }
    
    public Actor returnTo(Actor from, Actor to) {
        addInvoke(new Return(from, to));
        return to;
    }

    public Actor returnTo(Actor from, Actor to, int height) {
        addInvoke(new Return(from, to, height));
        return to;
    }
   
    public int getY() {
        return y;
    }

    public void generate() {
        try {
            Map<String, Object> vars = new HashMap<String, Object>();
            
            ParserContext context = new ParserContext();
            CompiledTemplate templ = TemplateCompiler.compileTemplate(getClass().getResourceAsStream("/sequenceDiag.mvel"), context);
            FileOutputStream out = new FileOutputStream("target/" + title + ".svg");
            TemplateRuntime.execute(templ, this, vars, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public List<Actor> getActors() {
        return actors;
    }

    public List<Invoke> getCalls() {
        return calls;
    }

}
