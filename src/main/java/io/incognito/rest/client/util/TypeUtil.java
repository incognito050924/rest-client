package io.incognito.rest.client.util;

import java.util.Arrays;
import java.util.stream.Stream;

import lombok.NonNull;

public class TypeUtil {

    /**
     * 해당 클래스의 객체가 특정 클래스 타입에 할당 가능한지 확인한다.
     *
     * @param clazz 확인할 클래스 객체
     * @param candidateClass 비교할 타입의 클래스 객체
     * @param <S> 할당 가능한지 체크할 타입
     * @return 할당 가능 여부
     */
    public static <S> boolean isAssignableTypeOf(@NonNull final Class<?> clazz, @NonNull final Class<S> candidateClass) {
        final Opt<Class<?>> superClassOpt = Opt.of(clazz.getSuperclass());
        if (clazz.isInterface()) {
            return !isSubInterfaceOf(clazz, candidateClass);
        } else if (isAssignableFrom(clazz, candidateClass)) {
            return true;
        } else if (superClassOpt.isPresent()) {
            if (superClassOpt.filter(superClass -> isAssignableFrom(superClass, candidateClass)).isPresent()) {
                return true;
            } else {
                return isAssignableTypeOf(superClassOpt.get(), candidateClass);
            }
        }
        return false;
    }

    private static boolean isAssignableFrom(@NonNull final Class<?> clazz, @NonNull final Class<?> candidateClass) {
        return clazz.equals(candidateClass) || (candidateClass.isAssignableFrom(clazz) && clazz.isAssignableFrom(candidateClass)) || isSubInterfaceOf(clazz, candidateClass);
    }

    public static <I> boolean isSubInterfaceOf(@NonNull final Class<?> clazz, @NonNull final Class<I> superClass) {
        if (!superClass.isInterface()) {
            return false;
        }

        if (superClass.isAssignableFrom(clazz) ) {
            return true;
        } else {
            return Arrays.asList(clazz.getInterfaces()).contains(superClass) || Stream.of(clazz.getInterfaces()).anyMatch(i -> isSubInterfaceOf(i, superClass));
        }
    }
}
