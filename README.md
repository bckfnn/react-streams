react-streams
=============

A reactive-extension library for java inspired by the http://reactive-streams.org initiative.

The core interfaces used (Publisher/Subscriber/Subscription) are taken directly from reactive-streams.org, but the contract of the interfaces are slightly (mis)used. 
**react-stream** connects each step in a procesing pipeline through Publisher and Subscriber but unlike reactive-streams, does not mandate that back pressure events must be asynchronious. Instead the back-pressure must be non-recursive.
**react-stream** steps can only support a single subscriber. If multiple subscribers are needed, a delegate() step must added.


### Travis

![Travis Status](https://travis-ci.org/bckfnn/react-streams.svg?branch=master)

### License

Apache License, Version 2.0

### Comparing with other techs.

Unlike other great reactive extension libraries such as RxJava and reactor, react-stream assume single thread use.

Back pressure is build in to every processing step.
