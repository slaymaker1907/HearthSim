package com.hearthsim.util.immutable;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ImmutableMap<Key, Value> implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;
    private final HashMap<Key, Value> data;
    
    public ImmutableMap()
    {
        data = new HashMap<>();
    }
    
    public ImmutableMap(Map<? extends Key, ? extends Value> map)
    {
        data = new HashMap<>(map);
    }
    
    public ImmutableMap(Collection<? extends Key> keys, Function<? super Key, ? extends Value> functionMap)
    {
        data = new HashMap<>();
        for(Key key : keys)
            data.put(key, functionMap.apply(key));
    }
    
    // This is an unsafe constructor that should only be used internally as a way to efficiently create ImmutableMaps for internal functions.
    // It is assumed that data is already unique and this is provided as a way to prevent double copying.
    private ImmutableMap(HashMap<Key, Value> data, boolean unsafeFlag)
    {
        // The boolean flag is solely to change the method signature and so its value has no meaning.
        this.data = data;
    }
    
    @Override
    public Object clone()
    {
        return new HashMap<>(data);
    }
    
    public ImmutableMap<Key, Value> compute(Key key, BiFunction<? super Key, ? super Value, ? extends Value> remappingFunction)
    {
        HashMap<Key, Value> result = new HashMap<>(data);
        result.compute(key, remappingFunction);
        return new ImmutableMap<Key, Value>(result, true);
    }
    
    public ImmutableMap<Key, Value> computeIfAbsent(Key key, Function<? super Key, ? extends Value> mappingFunction)
    {
        HashMap<Key, Value> result = new HashMap<>(data);
        result.computeIfAbsent(key, mappingFunction);
        return new ImmutableMap<Key, Value>(result, true);
    }
    
    public ImmutableMap<Key, Value> computeIfPresent(Key key, BiFunction<? super Key, ? super Value, ? extends Value> remappingFunction)
    {
        HashMap<Key, Value> result = new HashMap<>(data);
        result.computeIfPresent(key, remappingFunction);
        return new ImmutableMap<Key, Value>(result, true);
    }
    
    public boolean containsKey(Object key)
    {
        return data.containsKey(key);
    }
    
    public boolean containsValue(Object value)
    {
        return data.containsValue(value);
    }
    
    public Set<Map.Entry<Key, Value>> entrySet()
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        return copy.entrySet();
    }
    
    public void forEach(BiConsumer<? super Key, ? super Value> action)
    {
        data.forEach(action);
    }
    
    public Value get(Object key)
    {
        return data.get(key);
    }
    
    public Value getOrDefault(Object key, Value defaultValue)
    {
        return data.getOrDefault(key, defaultValue);
    }
    
    public boolean isEmpty()
    {
        return data.isEmpty();
    }
    
    public Set<Key> keySet()
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        return copy.keySet();
    }
    
    public ImmutableMap<Key, Value> merge(Key key, Value value, BiFunction<? super Value, ? super Value,? extends Value> remappingFunction)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.merge(key, value, remappingFunction);
        return new ImmutableMap<Key, Value>(copy, true);
    }
    
    public ImmutableMap<Key, Value> put(Key key, Value value)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.put(key, value);
        return new ImmutableMap<Key, Value>(copy, true);
    }
    
    public ImmutableMap<Key, Value> putAll(Map<? extends Key, ? extends Value> map)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.putAll(map);
        return new ImmutableMap<Key, Value>(copy, true);
    }
    
    public ImmutableMap<Key, Value> putIfAbsent(Key key, Value value)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.putIfAbsent(key, value);
        return new ImmutableMap<Key, Value>(copy, true);
    }
    
    public ImmutableMap<Key, Value> remove(Object key)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.remove(key);
        return new ImmutableMap<>(copy, true);
    }
    
    public ImmutableMap<Key, Value> remove(Object key, Object value)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.remove(key, value);
        return new ImmutableMap<>(copy, true);
    }
    
    public ImmutableMap<Key, Value> replace(Key key, Value value)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.replace(key, value);
        return new ImmutableMap<>(copy, true);
    }
    
    public ImmutableMap<Key, Value> replace(Key key, Value oldValue, Value newValue)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.replace(key, oldValue, newValue);
        return new ImmutableMap<>(copy, true);
    }
    
    public ImmutableMap<Key, Value> replaceAll(BiFunction<? super Key, ? super Value, ? extends Value> function)
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        copy.replaceAll(function);
        return new ImmutableMap<>(copy, true);
    }
    
    public int size()
    {
        return data.size();
    }
    
    public Collection<Value> values()
    {
        HashMap<Key, Value> copy = new HashMap<>(data);
        return copy.values();
    }
}
