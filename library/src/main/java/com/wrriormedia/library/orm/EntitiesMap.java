package com.wrriormedia.library.orm;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 王先佑
 *         <p>
 *         这个类用于缓存实体，减少对数据库的访问
 *         </p>
 */
class EntitiesMap {
    private static EntitiesMap ENTITIES_MAP = new EntitiesMap();

    private Map<String, WeakReference<BaseModel>> mTypeModelMap = new HashMap<>();

    static EntitiesMap instance() {
        return ENTITIES_MAP;
    }

    @SuppressWarnings("unchecked")
    <T extends BaseModel> T get(Class<T> c, long id) {
        String key = makeKey(c, id);
        WeakReference<BaseModel> i = mTypeModelMap.get(key);
        if (i == null) {
            return null;
        }
        return (T) i.get();
    }

    void set(BaseModel e) {
        String key = makeKey(e.getClass(), e.getID());
        mTypeModelMap.put(key, new WeakReference<>(e));
    }

    private String makeKey(Class<?> entityType, long id) {
        StringBuilder sb = new StringBuilder();
        sb.append(entityType.getName()).append(id);
        return sb.toString();
    }
}
