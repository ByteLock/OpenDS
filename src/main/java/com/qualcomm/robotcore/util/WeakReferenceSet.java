package com.qualcomm.robotcore.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakReferenceSet<E> implements Set<E> {
    WeakHashMap<E, Integer> members = new WeakHashMap<>();

    public boolean add(E e) {
        boolean z;
        synchronized (this.members) {
            z = true;
            if (this.members.put(e, 1) != null) {
                z = false;
            }
        }
        return z;
    }

    public boolean remove(Object obj) {
        boolean z;
        synchronized (this.members) {
            z = this.members.remove(obj) != null;
        }
        return z;
    }

    public boolean contains(Object obj) {
        boolean containsKey;
        synchronized (this.members) {
            containsKey = this.members.containsKey(obj);
        }
        return containsKey;
    }

    public boolean addAll(Collection<? extends E> collection) {
        boolean z;
        synchronized (this.members) {
            z = false;
            for (Object add : collection) {
                if (add(add)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public void clear() {
        synchronized (this.members) {
            this.members.clear();
        }
    }

    public boolean containsAll(Collection<?> collection) {
        synchronized (this.members) {
            for (Object contains : collection) {
                if (!contains(contains)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        int size;
        synchronized (this.members) {
            size = this.members.size();
        }
        return size;
    }

    public Object[] toArray() {
        Object[] array;
        synchronized (this.members) {
            LinkedList linkedList = new LinkedList();
            for (E add : this.members.keySet()) {
                linkedList.add(add);
            }
            array = linkedList.toArray();
        }
        return array;
    }

    public Iterator<E> iterator() {
        Iterator<E> it;
        synchronized (this.members) {
            LinkedList linkedList = new LinkedList();
            for (E add : this.members.keySet()) {
                linkedList.add(add);
            }
            it = linkedList.iterator();
        }
        return it;
    }

    public boolean removeAll(Collection<?> collection) {
        boolean z;
        synchronized (this.members) {
            z = false;
            for (Object remove : collection) {
                if (remove(remove)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public boolean retainAll(Collection<?> collection) {
        boolean z;
        synchronized (this.members) {
            z = false;
            Iterator it = iterator();
            while (it.hasNext()) {
                Object next = it.next();
                if (!collection.contains(next) && remove(next)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public Object[] toArray(Object[] objArr) {
        synchronized (this.members) {
            Object[] array = toArray();
            if (array.length > objArr.length) {
                objArr = new Object[array.length];
            }
            int i = 0;
            while (i < array.length) {
                objArr[i] = array[i];
                i++;
            }
            while (i < objArr.length) {
                objArr[i] = null;
                i++;
            }
        }
        return objArr;
    }
}
