package org.sana.android.service.impl.test;

import android.content.Intent;
import android.test.ServiceTestCase;

import org.sana.android.provider.Observations;
import org.sana.android.service.impl.InstrumentationService;

/**
 *
 */
public class InstrumentationServiceTest extends ServiceTestCase<InstrumentationService> {
    public static final String TAG = InstrumentationServiceTest.class.getSimpleName();

    /**
     * Constructor
     *
     * @param serviceClass The type of the service under test.
     */
    public InstrumentationServiceTest(Class<InstrumentationService> serviceClass) {
        super(serviceClass);
    }

    public InstrumentationServiceTest() {
        super(InstrumentationService.class);
    }

    public void testStartStopService(){
        Intent intent = new Intent(getContext(),InstrumentationService.class);
        startService(intent);
        getContext().stopService(intent);
    }
}
