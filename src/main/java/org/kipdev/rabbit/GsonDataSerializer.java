package org.kipdev.rabbit;

import com.google.common.collect.Maps;
import net.pixelverse.gson.SuperGson;
import net.pixelverse.gson.SuperGsonBuilder;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Simple data serializer that can be easily expanded upon and does not lose any type accuracy.
 *
 * If more speed or capabilities are required then this can be overridden in RabbitMessageController.
 */
public enum GsonDataSerializer implements DataSerializer {
    INSTANCE;

    private final Map<Type, Object> compatibilities = Maps.newHashMap();
    private final Map<Class<?>, Object> hierarchyCompatibilities = Maps.newHashMap();

    private SuperGson gson = new SuperGson();

    @Override
    public <T> T parse(byte[] data) {
        return gson.fromJson(new String(data, StandardCharsets.UTF_8));
    }

    @Override
    public <T> byte[] write(T data) {
        return gson.toJson(data).getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unused")
    public void registerTypeAdapter(Type type, Object adapter) {
        compatibilities.put(type, adapter);
        recompileGson();
    }

    @SuppressWarnings("unused")
    public void registerTypeHierarchyAdapter(Class<?> type, Object adapter) {
        hierarchyCompatibilities.put(type, adapter);
        recompileGson();
    }

    private void recompileGson() {
        SuperGsonBuilder builder = (SuperGsonBuilder) new SuperGsonBuilder().serializeSpecialFloatingPointValues();

        for (Map.Entry<Type, Object> entry : compatibilities.entrySet()) {
            builder.registerTypeAdapter(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Class<?>, Object> entry : hierarchyCompatibilities.entrySet()) {
            builder.registerTypeHierarchyAdapter(entry.getKey(), entry.getValue());
        }
        gson = builder.create();
    }
}
