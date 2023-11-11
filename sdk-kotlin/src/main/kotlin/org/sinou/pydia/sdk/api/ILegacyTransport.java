package org.sinou.pydia.sdk.api;

import java.io.InputStream;

public interface ILegacyTransport extends Transport {

    InputStream getCaptcha() throws SDKException;

    boolean useCaptcha();

}
