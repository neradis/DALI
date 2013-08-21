package de.uni_leipzig.mack.persistence.testing

import de.uni_leipzig.mack.persistence.instrumentation.Db4oIntrument
import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@Db4oIntrument
@CompileStatic
class SimpleLinkedList<T> {
    Node<T> head

    SimpleLinkedList(Collection<T> collection) {
        for (item in collection) {
            append(item)
        }
    }

    T get(int index) {
        def i = 0i
        def pos = getHead()
        while (!pos.is(null) && i < index) {
            pos = pos.getNext(); i++
        }
        pos?.item
    }

    T getLast() {
        getLastNode().getItem()
    }

    void setLast(T item) {
        getLastNode().setItem(item)
    }

    protected Node<T> getLastNode() {
        def pos = head
        while (!pos.is(null) && !pos.getNext().is(null)) {
            pos = pos.getNext()
        }
        pos
    }

    protected append(T item) {
        if (head.is(null)) {
            head = new Node(null, item)
        } else {
            getLastNode().setNext(new Node(null, item))
        }
    }

    int size() {
        int size = 0
        Node<T> pos = head
        while (!pos.is(null)) {
            pos = pos.getNext()
            size++
        }
        size
    }

    @Db4oIntrument
    @Canonical
    public static class Node<T> { //
        Node<T> next
        T item
    }
}