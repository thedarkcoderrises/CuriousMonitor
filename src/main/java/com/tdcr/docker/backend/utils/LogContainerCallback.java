package com.tdcr.docker.backend.utils;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.util.LinkedList;
import java.util.List;

public class LogContainerCallback  extends ResultCallbackTemplate<LogContainerResultCallback, Frame> {

    private final List<String> entries = new LinkedList<>();
    @Override
    public void onNext(Frame frame) {
        entries.add(getEntryMessage(frame));
    }

    /**
     * Gets the message contents from the {@link Frame}.
     *
     * @return the message contents.
     */
    private String getEntryMessage(Frame frame) {
        String string = new String(frame.getPayload());
        return string;
    }


    public List<String> getEntries() {
        return entries;
    }
}
