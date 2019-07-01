package com.swissas.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * override the properties class to store elements in the insert order
 * this is needed in order to prevent changing the entire properties file just to add one translation
 *
 * @author Tavan Alain
 */

public class SequenceProperties extends Properties {

	private final Set<Object> keySet = new LinkedHashSet<>(100);

	@Override
	public Enumeration<Object> keys() {
		return Collections.enumeration(this.keySet);
	}

	@Override
	public Set<Object> keySet() {
		return this.keySet;
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		this.keySet.add(key);
		return super.put(key, value);
	}

	@Override
	public synchronized Object remove(Object key) {
		this.keySet.remove(key);
		return super.remove(key);
	}

	@Override
	public synchronized void putAll(Map values) {
		this.keySet.addAll(values.keySet());
		super.putAll(values);
	}

	@Override
	public synchronized Set<Map.Entry<Object, Object>> entrySet() {
		return Collections.synchronizedSet(
				this.keySet.stream().map(k -> new MyEntry<>(k, get(k))).collect(Collectors.toCollection(LinkedHashSet::new))
		);
	}
	
	final class MyEntry<K, V> implements Map.Entry<K, V>{
		private final K key;
		private V value;

		MyEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V value) {
			V old = this.value;
			this.value = value;
			return old;
		}
	}
}