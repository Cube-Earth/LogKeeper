package org.slf4j.impl;


import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import earth.cube.tools.logkeeper.loggers.slf4j.ZmqLoggerFactory;


public class StaticLoggerBinder implements LoggerFactoryBinder {

	//{{ class members
	
    // To avoid constant folding by the compiler,
    // this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.6";  // !final

    private static final StaticLoggerBinder INSTANCE = new StaticLoggerBinder();

    
    public static final StaticLoggerBinder getSingleton() {
        return INSTANCE;
    }
    
    //}}
    
    //{{ instance members

    private final ILoggerFactory _factory = new ZmqLoggerFactory();


    public ILoggerFactory getLoggerFactory() {
        return _factory;
    }

    public String getLoggerFactoryClassStr() {
        return _factory.getClass().getCanonicalName();
    }
    
    //}}
}