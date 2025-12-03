package com.paulmojicatech;

import com.getcapacitor.Logger;

public class NativeClipboard {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
