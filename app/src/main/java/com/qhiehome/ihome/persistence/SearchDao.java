package com.qhiehome.ihome.persistence;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by YueMa on 2017/8/16.
 */
@Entity
public class SearchDao {
    @Id
    private long id;
    private String name;
    @Transient
    private int tempUsageCount; // not persisted

    @Generated(hash = 207259576)
    public SearchDao(long id, String name) {
        this.id = id;
        this.name = name;
    }
    @Generated(hash = 1159997855)
    public SearchDao() {
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}