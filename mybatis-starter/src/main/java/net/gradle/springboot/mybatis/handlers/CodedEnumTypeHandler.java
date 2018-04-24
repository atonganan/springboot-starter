package net.gradle.springboot.mybatis.handlers;

import net.gradle.springboot.beans.enums.CodedEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 带有code的枚举类型处理器.
 * 枚举类需要实现CodedEnum接口，并且提供一个<code>&lt;E extends CodedEnum&gt; E translate(T CODE)</code>的方法以实现初始化.
 */
public class CodedEnumTypeHandler<T> extends BaseTypeHandler<CodedEnum<T>> {
    protected final Class<? extends CodedEnum> actualType;
    protected final Type codedEnumRawType;
    protected final Method translateMethod;

    public CodedEnumTypeHandler(Class<? extends CodedEnum> actualType) {
        this.actualType = actualType;
        codedEnumRawType = ((ParameterizedType)actualType.getGenericInterfaces()[0]).getActualTypeArguments()[0];
        try {
            translateMethod = actualType.getMethod("translate", (Class<?>) codedEnumRawType);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class["+actualType+"] does not define a static method [public static "+actualType+" translate("+((Class<?>) codedEnumRawType).getName()+")] for instance initialization");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CodedEnum<T> parameter, JdbcType jdbcType) throws SQLException {
        if(codedEnumRawType.equals(String.class)){
            ps.setString(i,(String) parameter.getCode());
        }else{
            ps.setInt(i,(Integer) parameter.getCode());
        }
    }

    @Override
    public CodedEnum<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return newInstance((T) (codedEnumRawType.equals(String.class) ?
                rs.getString(columnName) : rs.getInt(columnName)));
    }

    @Override
    public CodedEnum<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return newInstance((T) (codedEnumRawType.equals(String.class) ? rs.getString(columnIndex) : rs.getInt(columnIndex)));
    }

    @Override
    public CodedEnum<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return newInstance((T) (codedEnumRawType.equals(String.class) ? cs.getString(columnIndex) : cs.getInt(columnIndex)));
    }


    protected CodedEnum<T> newInstance(T val){
        try {
            return (CodedEnum<T>) translateMethod.invoke(actualType,val);
        } catch (Exception e) {
            throw new RuntimeException("Error creating new instance to "+actualType);
        }
    }
}