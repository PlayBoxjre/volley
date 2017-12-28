package com.playboxjre.opensource.project.android.volley.interfaces.cache;

import com.playboxjre.opensource.project.android.volley.core.Header;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An interface for a cache keyed by a String with a byte array as data.
 * 一个以字节数组作为数据键的高速缓冲存储器的接口。
 */
public interface Cache {

    /**
     * 获取指定的缓存实体
     * @param key
     * @return
     */
    Entry get(String key);

    /**
     * Adds or replaces an entry to the cache.
     * 添加或缓存实体
     * @param key
     * @param entry
     */
    void put(String key, Entry entry);

    /**
     * 执行初始化缓存所需的任何潜在的长时间操作；将从工作线程调用该操作。
     */
    void initialize();

    /**
     * Invalidates an entry in the cache.
     * 使高速缓存条目无效
     * @param key Cache key
     * @param fullExpire True to fully expire the entry, false to soft expire
     */
    void invalidate(String key,boolean fullExpire);

    /**
     * Removes an entry from the cache.
     * @param key Cache key
     */
    void remove(String key);

    /** Empties the cache */
    void clear();
    /**
     * Data and metadata for an entry returned by the cache.
     */
    class Entry{
        /** The data returned from cache. */
        public byte[] data;

        /** ETag for cache coherency. */
        public String etag;

        /** Date of this response as reported by the server. */
        public long serverDate;

        /** The last modified date for the requested object. */
        public long lastModified;

        /** TTL for this record. */
        public long ttl;

        /** Soft TTL for this record. */
        public long softTtl;

        /**
         * Response headers as received from server; must be non-null. Should not be mutated
         * directly.
         *
         * <p>Note that if the server returns two headers with the same (case-insensitive) name,
         * this map will only contain the one of them. {@link #allResponseHeaders} may contain all
         * headers if the {@link Cache} implementation supports it.
         */
        public Map<String,String> responseHeaders = Collections.emptyMap();

        /**
         * All response headers. May be null depending on the {@link Cache} implementation. Should
         * not be mutated directly.
         */
        public List<Header>  allResponseHeaders;

        /** True if the entry is expired. */
        public boolean isExpired(){
            return this.ttl < System.currentTimeMillis();
        }

        /** True if a refresh is needed from the original data source. */
        public boolean refreshNeeded() {
            return this.softTtl < System.currentTimeMillis();
        }
    }
}
