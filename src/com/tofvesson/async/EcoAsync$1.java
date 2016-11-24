package com.tofvesson.async;

import java.lang.reflect.Method;

/**
 * Pre-written anonymous class. Follows all naming conventions of an anonymous class, acts like an anonymous class and handles data like an anonymous class.
 * It just isn't an anonymous class technically speaking...
 */
class EcoAsync$1 implements Runnable{

    private final EcoAsync this$0;
    private final Object val$o;
    private final Method val$method;
    private final Object[] val$params;

    EcoAsync$1(EcoAsync this$0, Object val$o, Method val$method, Object[] val$params){
        this.this$0 = this$0;
        this.val$o = val$o;
        this.val$method = val$method;
        this.val$params = val$params;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                this$0.setLocal();
                this$0.ret = val$method.invoke(val$o, val$params);
                this$0.complete = true;
            } catch (Throwable t1) {
                if(!this$0.failed)
                {
                    this$0.failed = true; this$0.t=t1;
                }
            } finally {
                this$0.newThread(val$o, val$method, val$params);
            }
        }
    }
}
