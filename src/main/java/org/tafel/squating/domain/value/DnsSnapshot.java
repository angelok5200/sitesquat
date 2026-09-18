package org.tafel.squating.domain.value;

import java.util.List;

public record DnsSnapshot (
    List<String> a,
    List<String> aaa,
    List<String> txt,
    List<String> mx,
    List<String> ns,
    List<String> cname
){
    public DnsSnapshot{
        a = a != null ? List.copyOf(a) : List.of();
        aaa = aaa != null ? List.copyOf(aaa) : List.of();
        cname = cname != null ? List.copyOf(cname) : List.of();
        mx = mx != null ? List.copyOf(mx) : List.of();
        ns = ns != null ? List.copyOf(ns) : List.of();
        txt = txt != null ? List.copyOf(txt) : List.of();
    }
}

