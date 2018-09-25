package club.tempvs.message.util;

public interface ObjectFactory {
    <T> T getInstance(Class<T> clazz, Object... args);
}
