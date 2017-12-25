package com.playboxjre.opensource.project.android.volley.core;

import java.util.Objects;

/** An HTTP header.
 *  一个 http请求的Header类
 */
public final class Header {
    private final String name;
    private final String value;

    public Header(String name,String value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        Header header = ((Header) obj);
        return Objects.equals(name,header.name)&&Objects.equals(value,header.value);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Header{" +
                "mName='" + name + '\'' +
                ", mValue='" + value + '\'' +
                '}';
    }
}
