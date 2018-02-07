package com.shenit.springboot.mybatis.handlers;

import com.shenit.commons.utils.GsonUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Json变Map的处理器.
 */
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {
    private final Class<T> actualClass;

    public JsonTypeHandler(Class<T> actualClass) {
        this.actualClass = actualClass;
    }


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, GsonUtils.format(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return GsonUtils.parse(rs.getString(columnName),actualClass);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return GsonUtils.parse(rs.getString(columnIndex),actualClass);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return GsonUtils.parse(cs.getString(columnIndex),actualClass);
    }
}
