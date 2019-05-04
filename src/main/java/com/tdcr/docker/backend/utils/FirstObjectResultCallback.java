package com.tdcr.docker.backend.utils;

import com.github.dockerjava.core.async.ResultCallbackTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class FirstObjectResultCallback<E> extends ResultCallbackTemplate<FirstObjectResultCallback<E>, E> {

    private E object;
    private CountDownLatch latch = new CountDownLatch(1);
    private static Logger LOG = LoggerFactory.getLogger(FirstObjectResultCallback.class);
    @Override
    public void onNext(E object) {
        this.object = object;
        latch.countDown();
        try {
            close();
        } catch (IOException e) {
            LOG.warn("Exception when closing stats cmd stream", e);
        }
    }

    public E waitForObject() throws InterruptedException {
        latch.await();
        return object;
    }
}
