/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.net.http;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyStore;
import java.util.Map;


import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.sana.net.http.ssl.EasySSLSocketFactory;

/**
 * @author Sana Development
 *
 */
public class HttpTaskFactory {

    /* Basic implementation of a Factory */
    public static EntityFactory ENTITY_FACTORY = new EntityFactory(){
        /*
         * (non-Javadoc)
         * @see org.sana.net.http.HttpFactory.EntityFactory#produce(java.util.Map)
         */
        @Override
        public HttpEntity produce(Map<String, ?> formData)
                throws UnsupportedEncodingException {
            return produce(formData, null);
        }
        /*
         * (non-Javadoc)
         * @see org.sana.net.http.HttpFactory.EntityFactory#produce(java.util.Map, java.util.Map)
         */
        @Override
        public HttpEntity produce(Map<String, ?> formData,
                Map<String, URI> files) throws UnsupportedEncodingException {
            MultipartEntity entity = new MultipartEntity();
            if(files != null){
                for(String key:files.keySet()){
                    URI value = files.get(key);
                    entity.addPart(key, new FileBody(new File(value)));
                }
            }
            if(formData != null){
                for(String key:formData.keySet()){
                    Object value = formData.get(key);
                    entity.addPart(key, new StringBody(String.valueOf(value)));
                }
            }
            return entity;
        }
        
    };
    
    /* Basic implementation of a Factory */
    public static RequestFactory REQUEST_FACTORY = new RequestFactory(){
        /*
         * (non-Javadoc)
         * @see com.sana.android.net.HttpThread.Factory#produceCreate(java.net.URI, java.util.Map, java.util.Map)
         */
        @Override
        public HttpUriRequest produceCreate(URI uri, Map<String, ?> formData,
                Map<String, URI> files) throws UnsupportedEncodingException {
            HttpPost request = new HttpPost(uri);
            HttpEntity entity = ENTITY_FACTORY.produce(formData, files);
            request.setEntity(entity);
            return request;
        }
        
        /*
         * (non-Javadoc)
         * @see com.sana.android.net.HttpThread.Factory#produceRead(java.net.URI)
         */
        @Override
        public HttpUriRequest produceRead(URI uri) {
            return new HttpGet(uri);
        }
        /*
         * (non-Javadoc)
         * @see com.sana.android.net.HttpThread.Factory#produceUpdate(java.net.URI, java.util.Map, java.util.Map)
         */
        @Override
        public HttpUriRequest produceUpdate(URI uri, Map<String, ?> formData,
                Map<String, URI> files) throws UnsupportedEncodingException {
            HttpPut request = new HttpPut(uri);
            HttpEntity entity = ENTITY_FACTORY.produce(formData, files);
            request.setEntity(entity);
            return request;
        }
        /*
         * (non-Javadoc)
         * @see com.sana.android.net.HttpThread.Factory#produceDelete(java.net.URI)
         */
        @Override
        public HttpUriRequest produceDelete(URI uri){
            return new HttpDelete(uri);
        }
    };
    
    public static ClientFactory CLIENT_FACTORY = new ClientFactory(){

        @Override
        public HttpClient produce() {
            //Set up your HTTPS connection
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            // http scheme
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            // https scheme
            schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
            
            HttpParams params = basicParams();
            ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
            return new DefaultHttpClient(cm, params);
        }

        @Override
        public HttpClient produce(InputStream keystore, String keypass) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(keystore, keypass.toCharArray());

                SSLSocketFactory sf = SSLSocketFactory.getSocketFactory();
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                HttpParams params = basicParams();
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", sf, 443));

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

                return new DefaultHttpClient(ccm, params);
            } catch (Exception e) {
                return new DefaultHttpClient();
            }
        }
        
        protected HttpParams basicParams(){
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            // Make sure this connection will timeout
            HttpConnectionParams.setConnectionTimeout(params, 300000);
            HttpConnectionParams.setSoTimeout(params, 300000);
            return params;
        }
        
    };
    
    private HttpTaskFactory() {}

    /**
     * 
     * @param uri
     * @param formData
     * @param files
     * @param handler
     * @return
     */
    public static <K> HttpThread<K> produceCreate(URI uri, Map<String, ?> formData,
            Map<String, URI> files, ResponseHandler<K> handler)  {
        HttpUriRequest request = null;
        try {
            request = REQUEST_FACTORY.produceCreate(uri, formData, files);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        HttpClient client = CLIENT_FACTORY.produce();
        return new HttpThread<K>(client, request,handler);
    }

    /**
     * 
     * @param uri
     * @param handler
     * @return
     */
    public static <K> HttpThread<K> produceRead(URI uri, ResponseHandler<K> handler)  {
        HttpUriRequest request = REQUEST_FACTORY.produceRead(uri);
        HttpClient client = CLIENT_FACTORY.produce();
        return new HttpThread<K>(client, request,handler);
    }

    /**
     * 
     * @param uri
     * @param formData
     * @param files
     * @param handler
     * @return
     */
    public static <K> HttpThread<K> produceUpdate(URI uri, Map<String, ?> formData,
            Map<String, URI> files, ResponseHandler<K> handler)  {
        HttpUriRequest request = null;
        try {
            request = REQUEST_FACTORY.produceUpdate(uri, formData, files);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }

        HttpClient client = CLIENT_FACTORY.produce();
        return new HttpThread<K>(client, request,handler);
    }

    /**
     * 
     * @param uri
     * @param handler
     * @return
     */
    public static <K> HttpThread<K> produceDelete(URI uri, ResponseHandler<K> handler)  {
        HttpUriRequest request = REQUEST_FACTORY.produceDelete(uri);
        HttpClient client = CLIENT_FACTORY.produce();
        return new HttpThread<K>(client, request,handler);
    }

}
