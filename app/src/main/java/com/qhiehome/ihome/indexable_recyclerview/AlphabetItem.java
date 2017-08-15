package com.qhiehome.ihome.indexable_recyclerview;

/**
 * Created by YueMa on 2017/8/15.
 */

public class AlphabetItem {
    public int position;
    public String word;
    public boolean isActive;

    public AlphabetItem(int pos, String word, boolean isActive) {
        this.position = pos;
        this.word = word;
        this.isActive = isActive;
    }
}
