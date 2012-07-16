package com.github.rmannibucau.cdi.camel.bean;

import com.github.rmannibucau.cdi.camel.scope.ExchangeScoped;

@ExchangeScoped
public class Bean1 {
    public static Bean1 current = null;

    public Bean1() {
        current = this;
    }

    public String enclose(final String body) {
        return "[" + body + "]";
    }
}
