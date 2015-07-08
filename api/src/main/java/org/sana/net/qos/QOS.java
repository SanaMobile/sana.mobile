package org.sana.net.qos;

import java.util.Date;

/**
 * Transmission data sent from the client.
 */
public class QOS {
    public String source = null;
    public String target = null;
    public Date sent = new Date();
    public int sendCount = 1;
    public Date eventStart = new Date();
    public Date eventComplete = new Date();
    public Date received = new Date();
    public Date requestComplete = new Date();

    public QOS(){}

    public void increment(){
        sendCount++;
    }
}
