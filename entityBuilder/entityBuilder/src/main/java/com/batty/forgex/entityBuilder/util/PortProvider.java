package com.batty.forgex.entityBuilder.util;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class PortProvider {

    private static int  port = 9001;

    public static int getPort()
    {
        return port++;
    }
}
