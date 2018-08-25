package club.tempvs.message.util;

import org.springframework.stereotype.Component;

@Component
public class ObjectFactoryImpl implements ObjectFactory {

    @Override
    public <T> T getInstance(Class<T> clazz) {
        T instance = null;

        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }
}
