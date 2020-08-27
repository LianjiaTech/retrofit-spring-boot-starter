package com.github.lianjiatech.retrofit.spring.boot.util;

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author 陈添明
 */
public final class BeanExtendUtils {

    private BeanExtendUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 使用map填充bean实例的属性值
     * Use map to fill in the attribute values of the bean instance
     *
     * @param bean       需要填充的实例bean。The instance bean that needs to be filled
     * @param properties 属性参数Map。Property parameter Map
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
     * 为指定实例对象的指定属性赋值，待赋值的属性字段必须提供setter方法。
     * Assign a value to the specified attribute of the specified instance object, the attribute field to be assigned must provide a setter method.
     *
     * @param bean  需要设置属性的示例对象。the object that needs to set properties
     * @param name  属性字段的名称。The name of the field
     * @param value 属性字段的值。The value of the field
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
