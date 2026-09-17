package org.tafel.squating.domain.value;

import java.util.List;

public record DnsSnapshot (
    List<String> a,
    List<String> aaa,
    List<String> txt,
    List<String> mx,
    List<String> ns,
    List<String> cname
){}
