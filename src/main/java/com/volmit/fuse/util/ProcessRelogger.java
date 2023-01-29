package com.volmit.fuse.util;

import com.volmit.fuse.Fuse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.function.Consumer;

public class ProcessRelogger extends Thread {
    private final boolean error;
    private final InputStream input;
    private final Consumer<String> consumer;

    public ProcessRelogger(InputStream input, boolean error, Consumer<String> consumer) {
        this.input = input;
        this.error = error;
        this.consumer = consumer;
    }

    public ProcessRelogger(InputStream input, boolean error) {
        this(input, error, null);
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(input));
               String line;
                while ((line = reader.readLine()) != null) {
                    if(consumer != null) {
                        consumer.accept(line);
                    }

                     if(error) {
                         Fuse.err(line);
                     }

                     else {
                         Fuse.log(line);
                     }
                }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
