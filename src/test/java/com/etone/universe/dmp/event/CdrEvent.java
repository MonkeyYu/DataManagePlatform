package com.etone.universe.dmp.event;

/**
 * Created by Lanny on 2016-9-9.
 */
public class CdrEvent extends BaseEvent {

    String[] cols = new String[100];

    public void initialize(){
        for (int i = 0; i< cols.length; i ++){
            cols[i] = "column " + i;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< cols.length; i ++){
            sb.append(cols[i]);
            sb.append(",");
        }

        return sb.toString();
    }
}
