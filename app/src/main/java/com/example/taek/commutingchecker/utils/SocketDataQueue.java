package com.example.taek.commutingchecker.utils;

import java.util.ArrayList;

/**
 * Created by Taek on 2016-10-15.
 */

public class SocketDataQueue {
    private ArrayList<Object> queueArray; // 데이터 큐

    // 생성자
    public SocketDataQueue() {
        queueArray = new ArrayList<>();
    }

    public boolean isEmpty() {
        return queueArray.isEmpty();
    }

    public int size() {
        return queueArray.size();
    }

    public boolean contains(Object obj) {
        return queueArray.contains(obj);
    }

    public void insert(Object item) {
        queueArray.add(item);
    }

    public Object peek() {
        return queueArray.get(0);
    }

    public Object remove() {
        Object item = peek();
        queueArray.remove(0);
        return item;
    }
}
