package serialize.protobuf;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 首先是定义Schema，这个是因为Protostuff-Runtime实现了无需预编译对java bean进行protobuf序列化/反序列化的能力。
 * 我们可以把运行时的Schema缓存起来，提高序列化性能。
 */
public class SchemaCache {
    private static class SchemaCacheHolder{
        private static SchemaCache cache = new SchemaCache();
    }

    public static SchemaCache getInstance(){
        return SchemaCacheHolder.cache;
    }

    private Cache<Class<?>, Schema<?>> cache = CacheBuilder.newBuilder()
            .maximumSize(1024).expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private Schema<?> get(final Class<?> cls, Cache<Class<?>, Schema<?>> cache){
        try{
            return cache.get(cls, new Callable<RuntimeSchema<?>>() {
                @Override
                public RuntimeSchema<?> call() throws Exception {
                    return RuntimeSchema.createFrom(cls);
                }
            });
        } catch (ExecutionException e){
            return null;
        }
    }

    public Schema<?> get(final Class<?> cls){
        return get(cls, cache);
    }
}
