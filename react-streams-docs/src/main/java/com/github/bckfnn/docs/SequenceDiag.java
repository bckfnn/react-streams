package com.github.bckfnn.docs;


public class SequenceDiag {
    public Actor[] actors;
    public Invoke[] calls;

    public void makeSubscribeChain() {
        Diagram d = new Diagram("subscription-chain");
        Actor s = d.actor("Subscriber", 150);
        Actor p = d.actor("Publisher", 150);
        
        d.call(s, "subscribe", p);
        d.call(p, "onSubscribe(subscription)", s);
        d.returnTo(s, p).returnTo(s, 20);
        d.generate();
    }
    
    public void makeSubscribeActive() {
        Diagram d = new Diagram("subscription-active");
        Actor a = d.actor("Application", 100);
        Actor p = d.actor("Publisher", 60);
        Actor sub = d.actor("Subscription", 200);
        Actor s = d.actor("Subscriber", 170);
        
        a
        .call("p.subscribe(s)", p)
        .call("s.onSubscribe(sub)", s)
        .call("sub.request(long)", sub)
        .returnTo(s)
        .returnTo(p)
        .call("s.onNext()", s)
        .call("sub.request(long)", sub).returnTo(s)
        .returnTo(p)
        .call("s.onNext()", s)
        .call("sub.request(long)", sub).returnTo(s)
        .returnTo(p)
        .call("s.onComplete()", s)
        .returnTo(p)
        .returnTo(a);
        
        d.generate();
    }
    
        
    public static void main(String[] args) {
        new SequenceDiag();
    }
    public SequenceDiag() {
        makeSubscribeChain();
        makeSubscribeActive();
    }
}
