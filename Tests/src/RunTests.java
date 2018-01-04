import net.tofvesson.async.*;
import net.tofvesson.collections.*;
import net.tofvesson.reflection.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("ALL")
public class RunTests {
    public static void main(String[] args){
        asyncTest();
        collectionsTest();
        streamTest();
        reflectionTest();
    }

    public static void asyncTest(){
        final int batchSize = 24;
        final Random random = ThreadLocalRandom.current();
        final ProxiedValue<Boolean> success = new ProxiedValue<>(true);

        System.out.print("Async test: ");
        Async<Boolean> async = new Async<>(() -> Async.current().postReturn(true));

        try{
            if(async.await()!=Boolean.TRUE) throw new RuntimeException("Bad return value");
            System.out.println("Passed");
        }catch(Throwable t){
            System.out.println("Failed ("+(t.getMessage().length()==0?"No reason given":t.getMessage())+")");
        }

        System.out.print("AsyncBatch test: ");
        AsyncBatch<Integer> batch = new AsyncBatch<>(batchSize, i -> Async.current().postReturn(i));
        Map<Integer, Integer> map = batch.awaitAll();
        map.forEach((k, v) -> success.value |= k.equals(v));
        System.out.println(success.value?"Passed":"Failed");

        success.value = true;

        System.out.print("EcoAsync test: ");
        final String expected = "Hello Eco";
        EcoAsync<String> eco = new EcoAsync<>(false, SafeReflection.getConstructor(String.class, String.class), expected);
        try{
            eco.await();
            System.out.println("Failed (could await uninitialized async)");
        }catch(Exception e){
            eco.start();
            success.value |= expected.equals(eco.await());

            eco.start();
            success.value |= expected.equals(eco.await());

            System.out.println(success.value?"Passed":"Failed: (Awaited values did not match expected value)");
        }

        success.value = true;

        System.out.print("Worker thread test: ");

        WorkerThread thread = new WorkerThread(batchSize);
        thread.start();
        final Map<Long, Integer> check = new HashMap<>();
        final Method invoke = SafeReflection.getFirstMethod(Supplier.class, "get");
        for(int i = 0; i<batchSize; ++i){
            final int expect = random.nextInt();
            check.put(thread.push((Supplier<Integer>)()->expect, invoke), expect);
        }
        for(Long id : check.keySet()) success.value |= check.get(id).equals(thread.pop(id));

        thread.stopGraceful();

        System.out.println(success.value?"Passed":"Failed");
    }

    public static void collectionsTest(){
        // TODO: Implement
    }

    public static void streamTest(){
        // TODO: Implement
    }

    public static void reflectionTest(){
        // TODO: Implement
    }
}
