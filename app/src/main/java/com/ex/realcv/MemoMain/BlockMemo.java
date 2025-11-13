package com.ex.realcv.MemoMain;

import java.util.UUID;

public class BlockMemo implements java.io.Serializable{

    public enum Type {TODO, PARA}

    public String id = UUID.randomUUID().toString();
    public Type type = Type.PARA;
    public String text = "";
    public boolean checked = false;

    public static BlockMemo para(String t) {
        BlockMemo b = new BlockMemo();
        b.type = Type.PARA;
        b.text = t;
        return b;
    }

    public static BlockMemo todo(String t, boolean c) {
        BlockMemo b = new BlockMemo();
        b.type = Type.TODO;
        b.text = t;
        b.checked = c;
        return b;
    }
}