package io.incognito.rest.client.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

/**
 * Kotlin 호환성을 위한 유틸리티 클래스
 * Kotlin data class와 Java 리플렉션 간의 호환성 문제를 해결하기 위한 헬퍼 메서드들을 제공합니다.
 */
public class KotlinCompatibilityUtil {

    private static final String KOTLIN_METADATA_ANNOTATION = "kotlin.Metadata";

    /**
     * 클래스가 Kotlin으로 작성되었는지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return Kotlin 클래스인 경우 true
     */
    public static boolean isKotlinClass(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredAnnotations())
                     .anyMatch(annotation -> annotation.annotationType().getName().equals(KOTLIN_METADATA_ANNOTATION));
    }

    /**
     * Kotlin data class의 기본값이 있는 생성자를 찾습니다.
     * Kotlin 컴파일러가 생성하는 synthetic 생성자 또는 기본값 마스크를 가진 생성자를 찾습니다.
     *
     * @param clazz 검색할 클래스
     * @return 기본값이 있는 생성자 (Optional)
     */
    public static Optional<Constructor<?>> findKotlinConstructorWithDefaults(Class<?> clazz) {
        if (!isKotlinClass(clazz)) {
            return Optional.empty();
        }

        return Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(constructor -> {
                        // Kotlin 컴파일러가 생성하는 synthetic 생성자 찾기
                        // (기본값이 있는 매개변수를 위한)
                        return constructor.isSynthetic() ||
                               hasDefaultValueMask(constructor);
                    })
                    .findFirst();
    }

    /**
     * 생성자가 Kotlin 기본값 마스크를 가지고 있는지 확인합니다.
     * Kotlin 기본값 생성자는 마지막 매개변수가 보통 int(마스크) 또는 Object(기본값) 타입입니다.
     *
     * @param constructor 확인할 생성자
     * @return 기본값 마스크를 가진 경우 true
     */
    private static boolean hasDefaultValueMask(Constructor<?> constructor) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        if (paramTypes.length < 2) {
            return false;
        }

        // Kotlin 기본값 생성자는 마지막 두 매개변수가 int(마스크)와 Object(기본값)인 경우가 많음
        int lastIndex = paramTypes.length - 1;
        int secondLastIndex = paramTypes.length - 2;

        return (paramTypes[lastIndex] == Object.class && paramTypes[secondLastIndex] == int.class) ||
               (paramTypes[lastIndex] == int.class);
    }

    /**
     * 기본값으로 매개변수 배열을 생성합니다.
     * 각 타입에 맞는 기본값을 제공합니다.
     *
     * @param paramTypes 매개변수 타입 배열
     * @return 기본값으로 채워진 매개변수 배열
     */
    public static Object[] createDefaultArgs(Class<?>[] paramTypes) {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = getDefaultValue(paramTypes[i]);
        }
        return args;
    }

    /**
     * 주어진 타입에 대한 기본값을 반환합니다.
     *
     * @param type 기본값을 구할 타입
     * @return 해당 타입의 기본값
     */
    public static Object getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0.0f;
            if (type == double.class) return 0.0d;
        }
        return null;
    }

    /**
     * Kotlin 호환성을 고려한 ObjectMapper를 생성합니다.
     * Jackson Kotlin 모듈이 클래스패스에 있을 경우 자동으로 등록하고,
     * Kotlin에서 자주 발생하는 직렬화/역직렬화 문제를 방지하는 설정을 적용합니다.
     *
     * @return Kotlin 호환성이 개선된 ObjectMapper
     */
    public static ObjectMapper createKotlinCompatibleMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Jackson Kotlin 모듈이 클래스패스에 있을 경우에만 등록
        try {
            Class.forName("com.fasterxml.jackson.module.kotlin.KotlinModule");
            mapper.findAndRegisterModules(); // KotlinModule 자동 등록
        } catch (ClassNotFoundException ignored) {
            // Kotlin 모듈이 없으면 기본 설정만 사용
        }

        // Kotlin 호환성을 위한 설정
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return mapper;
    }

    /**
     * 클래스의 인스턴스를 Kotlin 호환 방식으로 생성을 시도합니다.
     * 1. 기본 생성자 시도
     * 2. Kotlin 기본값이 있는 생성자 시도
     * 3. Jackson을 사용한 빈 객체 생성 시도
     *
     * @param clazz 인스턴스를 생성할 클래스
     * @param <T> 생성할 인스턴스의 타입
     * @return 생성된 인스턴스
     * @throws Exception 모든 방법이 실패한 경우
     */
    @SuppressWarnings("unchecked")
    public static <T> T createInstance(Class<T> clazz) throws Exception {
        // 1차: 기본 생성자 시도
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            // 2차: Kotlin 기본값이 있는 생성자 시도
            if (isKotlinClass(clazz)) {
                Optional<Constructor<?>> kotlinConstructor = findKotlinConstructorWithDefaults(clazz);
                if (kotlinConstructor.isPresent()) {
                    Constructor<?> constructor = kotlinConstructor.get();
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Object[] defaultArgs = createDefaultArgs(paramTypes);

                    constructor.setAccessible(true);
                    return (T) constructor.newInstance(defaultArgs);
                }

                // 3차: 매개변수가 있는 생성자 중에서 기본값으로 채워서 시도
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                for (Constructor<?> constructor : constructors) {
                    try {
                        Class<?>[] paramTypes = constructor.getParameterTypes();
                        Object[] defaultArgs = createDefaultArgs(paramTypes);
                        constructor.setAccessible(true);
                        return (T) constructor.newInstance(defaultArgs);
                    } catch (Exception ignored) {
                        // 다음 생성자 시도
                    }
                }
            }

            // 4차: Jackson을 사용한 빈 객체 생성
            try {
                ObjectMapper mapper = createKotlinCompatibleMapper();
                return mapper.readValue("{}", clazz);
            } catch (Exception jsonException) {
                throw new Exception("Failed to create instance using all available methods. " +
                    "Please ensure the class has a no-args constructor or proper Jackson annotations.", jsonException);
            }
        }
    }
}