package com.example.samsungshoeshoo;

import com.android.volley.ExecutorDelivery;

import java.util.concurrent.Executor;
/**
 * A ResponseDelivery for testing that immediately delivers responses
 * instead of posting back to the main thread.
 */
public class ImmediateResponseDelivery extends ExecutorDelivery {
    public ImmediateResponseDelivery() {
        super(new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });
    }
}