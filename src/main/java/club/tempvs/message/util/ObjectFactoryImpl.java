package club.tempvs.message.util;

import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Component
public class ObjectFactoryImpl implements ObjectFactory {

    @Override
    public <T> T getInstance(Class<T> clazz, Object... args) {
        Class[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        BiFunction<Class, Class, Boolean> typeMatcher = (constrType, invokedType) -> constrType.isAssignableFrom(invokedType);

        Predicate<Class[]> traverser = (Class[] classes) -> {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!typeMatcher.apply(classes[i], parameterTypes[i])) {
                    return false;
                }
            }

            return true;
        };

        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        Constructor<T> constructor = Arrays.stream(constructors)
                .filter(constr -> constr.getParameterCount() == parameterTypes.length)
                .filter(constr -> traverser.test(constr.getParameterTypes()))
                .findAny().get();

        T instance = null;

        try {
            instance = constructor.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }
}
