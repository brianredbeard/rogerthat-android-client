/*
 * Copyright 2018 GIG Technology NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @@license_version:1.4@@
 */
package com.mobicage.rogerthat.util.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// WARNING - this class is not threadsafe

public class SimpleCache<K, V> {

    private class Item {
        K key;
        V value;
        int hits;
        long expires;
    }

    private final Map<K, Item> map = new HashMap<K, Item>();
    private final List<Item> sorted = new ArrayList<Item>();
    private final int size;
    private final int timeoutSeconds;
    private final Comparator<Item> comparator = new Comparator<Item>() {
        @Override
        public int compare(Item item1, Item item2) {
            if (item1.hits == item2.hits)
                return 0;
            else
                return item1.hits > item2.hits ? 1 : -1;
        }
    };

    public SimpleCache(int size, int timeout) {
        this.size = size;
        this.timeoutSeconds = timeout;
    }

    public V get(K key) {
        Item item = map.get(key);
        if (item == null) {
            return null;
        }
        if (System.currentTimeMillis() > item.expires) {
            map.remove(key);
            sorted.remove(item);
            return null;
        }
        item.hits++;
        return item.value;
    }

    public void put(K key, V value) {
        Item item = map.get(key);
        long now = System.currentTimeMillis();
        if (item != null) {
            item.expires = now + (timeoutSeconds * 1000L);
            return;
        }
        if (sorted.size() == size) {
            for (int i = 0; i < sorted.size(); i++) {
                Item itm = sorted.get(i);
                if (itm.expires < now) {
                    map.remove(itm.key);
                    sorted.remove(itm);
                    break;
                }
            }
            if (sorted.size() == size) {
                Collections.sort(sorted, comparator);
                Item itm = sorted.get(0);
                sorted.remove(itm);
                map.remove(itm.key);
            }
        }
        item = new Item();
        item.key = key;
        item.value = value;
        item.hits = 0;
        item.expires = now + (timeoutSeconds * 1000L);
        sorted.add(0, item);
        map.put(key, item);
    }

    public void delete(K key) {
        map.remove(key);
    }

}
