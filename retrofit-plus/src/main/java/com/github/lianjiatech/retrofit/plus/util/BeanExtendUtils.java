package com.github.lianjiatech.retrofit.plus.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author 陈添明
 */
@UtilityClass
public class BeanExtendUtils {


    /**
     * 该方法是用于相同对象不同属性值的合并<br>
     * 如果两个相同对象中同一属性都有值，那么sourceBean中的值会覆盖tagetBean的值<br>
     * 如果sourceBean有值，targetBean没有，则采用sourceBean的值<br>
     * 如果sourceBean没有值，targetBean有，则保留targetBean的值
     *
     * @param sourceBean 被提取的对象bean
     * @param targetBean 用于合并的对象bean
     * @return 合并后的对象
     */
    @SneakyThrows
    public static <T> T combineProperties(T sourceBean, T targetBean) {
        Class sourceBeanClass = sourceBean.getClass();
        Class targetBeanClass = targetBean.getClass();

        Field[] sourceFields = sourceBeanClass.getDeclaredFields();
        Field[] targetFields = targetBeanClass.getDeclaredFields();
        for (int i = 0; i < sourceFields.length; i++) {
            Field sourceField = sourceFields[i];
            if (Modifier.isStatic(sourceField.getModifiers())) {
                continue;
            }
            Field targetField = targetFields[i];
            if (Modifier.isStatic(targetField.getModifiers())) {
                continue;
            }
            sourceField.setAccessible(true);
            targetField.setAccessible(true);
            if (!(sourceField.get(sourceBean) == null) && !"serialVersionUID".equals(sourceField.getName())) {
                targetField.set(targetBean, sourceField.get(sourceBean));
            }
        }
        return targetBean;
    }


    /**
     * 使用map填充bean实例的属性值
     *
     * @param bean       需要填充的实例bean
     * @param properties 属性参数Map
     */
    public static void populate(final Object bean, final Map<String, ?> properties) {
        // Do nothing unless both arguments have been specified
        if ((bean == null) || (properties == null)) {
            return;
        }
        // Loop through the property name/value pairs to be set
        for (final Map.Entry<String, ?> entry : properties.entrySet()) {
            // Identify the property name and value(s) to be assigned
            final String name = entry.getKey();
            if (name == null) {
                continue;
            }
            // Perform the assignment for this property
            setProperty(bean, name, entry.getValue());

        }
    }

    /**
     * 为指定实例对象的指定属性赋值。<br>
     * 待赋值的属性字段必须提供setter方法
     *
     * @param bean  需要设置属性的示例对象
     * @param name  属性字段的名称
     * @param value 属性字段的值
     */
    public static void setProperty(final Object bean, String name, final Object value) {
        Class<?> beanClass = bean.getClass();
        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(beanClass, name);
        if (propertyDescriptor == null) {
            return;
        }
        Method writeMethod = propertyDescriptor.getWriteMethod();
        try {
            writeMethod.invoke(bean, value);
        } catch (Exception e) {
            // skip
        }
    }
}
