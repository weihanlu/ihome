package com.qhiehome.ihome.persistence;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by YueMa on 2017/8/17.
 */
@Entity
public class MapSearch {
    @Id(autoincrement = true)
    private Long id;
    private String name;
    @Transient
    private int tempUsageCount; // not persisted
    @Generated(hash = 515128928)
    public MapSearch(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    @Generated(hash = 271113666)
    public MapSearch() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    
    


}
