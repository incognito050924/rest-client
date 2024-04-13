package io.incognito.rest.client.util;

import io.incognito.rest.client.types.dto.response.EmptyOrStringBodyResponse;
import io.incognito.rest.client.types.dto.response.IBaseResponse;
import lombok.NonNull;

public class TypeUtil {

    public static <S> boolean isSubTypeOf(@NonNull final Class<?> clazz, @NonNull final Class<S> superClass) {
        return isSubTypeOf(clazz, superClass, false);
    }

    /**
     * 해당 클래스가 특정 클래스의 서브 클래스인지 확인한다.
     *
     * @param clazz 확인할 클래스 객체
     * @param superClass 슈퍼 클래스 객체
     * @param implicitlySuperClass 정확하게 슈퍼 클래스로만 비교할지 여부 (true: 해당 클래스가 슈퍼 클래스와 같은 타입이어도 불일치 결과(false) 반환, false: 슈퍼 클래스와 같은 타입이면 일치 결과(true) 반환)
     * @param <S> 슈퍼 클래스 타입
     * @return 서브 클래스 여부
     */
    public static <S> boolean isSubTypeOf(@NonNull final Class<?> clazz, @NonNull final Class<S> superClass, final boolean implicitlySuperClass) {
        final Opt<Class<?>> superClazz = Opt.of(clazz.getSuperclass());
        if (clazz.isAssignableFrom(superClass)) {
            return !implicitlySuperClass;
        } else if (superClazz.isPresent()) {
            return superClazz.filter(superClass::isAssignableFrom).isPresent() || isSubTypeOf(superClazz.get(), superClass, implicitlySuperClass);
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isSubTypeOf(EmptyOrStringBodyResponse.class, IBaseResponse.class, true));
    }
}
