package club.tempvs.message.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ObjectFactoryImpl implements ObjectFactory {

    @Override
    public <T> T getInstance(Class<T> clazz, Object... args) {
        T instance = null;

        Class[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);

        try {
            instance = clazz.getConstructor(parameterTypes).newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }
}
