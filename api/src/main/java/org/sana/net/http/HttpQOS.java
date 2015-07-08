package org.sana.net.http;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.params.HttpParams;
import org.sana.net.qos.QOS;

import java.util.Date;

/**
 *
 */
public class HttpQOS {

    /**
     * Http header names for the transmission qos data. Parameter names are
     * designed to be consistent with default MDS implementation. MDS is
     * implemented in Django by default and hence usage of 'HTTP_' prefix and
     * QOS field names in camel case converted to lowercase with underscores
     * with subsequent capitalization.
     */
    public static interface Params {
        public static final String PREFIX = "HTTP_";
        public static final String SOURCE = PREFIX + "SOURCE";
        public static final String TARGET = PREFIX + "TARGET";
        public static final String SENT = PREFIX + "SENT";
        public static final String SEND_COUNT = PREFIX + "SEND_COUNT";
        public static final String EVENT_START = PREFIX + "EVENT_START";
        public static final String EVENT_COMPLETE = PREFIX + "EVENT_COMPLETE";
        public static final String RECEIVED = PREFIX + "RECEIVED";
        public static final String REQUEST_COMPLETE = PREFIX +
                "REQUEST_COMPLETE";
    }

    /**
     * Writes transmission quality data to an HttpRequest's headers.
     * @param request The HttpRequest to write to.
     * @param data The transmission data to write.
     */
    public static void write(HttpRequest request, QOS data) {
        HttpParams params = request.getParams();
        params.setParameter(Params.SOURCE, data.source);
        params.setParameter(Params.TARGET, data.target);
        if (data.sent != null){
            params.setLongParameter(Params.SENT, data.sent.getTime());
        }
        params.setIntParameter(Params.SEND_COUNT, data.sendCount);
        if (data.eventStart != null){
            params.setLongParameter(Params.EVENT_START,
                    data.eventStart.getTime());
        }
        if (data.eventComplete != null){
            params.setLongParameter(Params.EVENT_COMPLETE,
                    data.eventComplete.getTime());
        }
        if (data.received != null){
            params.setLongParameter(Params.RECEIVED, data.received.getTime());
        }
        if(data.requestComplete != null) {
            params.setLongParameter(Params.REQUEST_COMPLETE,
                    data.requestComplete.getTime());
        }
        request.setParams(params);
    }

    /**
     * Reads quality of service data from the response headers and returns a
     * new {@link org.sana.net.qos.QOS QOS} object.
     * @param response The object to read the qos data from.
     * @return A new
     */
    public static final QOS read(HttpResponse response){
        HttpParams params = response.getParams();
        QOS qos = new QOS();
        qos.source = String.valueOf(params.getParameter(Params.SOURCE));
        qos.target = String.valueOf(params.getParameter(Params.TARGET));
        qos.sent = new Date(params.getLongParameter(Params.SENT, 0));
        qos.sendCount = params.getIntParameter(Params.SEND_COUNT, 1);
        qos.eventStart = new Date(params.getLongParameter(Params.EVENT_START, 0));
        qos.eventComplete = new Date(params.getLongParameter(Params.EVENT_COMPLETE, 0));
        qos.received = new Date(params.getLongParameter(Params.RECEIVED, 0));
        qos.requestComplete = new Date(params.getLongParameter(Params.RECEIVED, 0));
        return qos;
    }
}
