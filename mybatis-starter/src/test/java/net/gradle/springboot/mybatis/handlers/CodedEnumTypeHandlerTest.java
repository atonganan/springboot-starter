package net.gradle.springboot.mybatis.handlers;

import net.gradle.springboot.beans.enums.EnabledStatus;
import org.easymock.EasyMock;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CodedEnumTypeHandlerTest extends EasyMockSupport {
    @Rule public EasyMockRule mocks = new EasyMockRule(this);
    @Mock private ResultSet rs;
    @Test
    public void testEnabledStatus() throws SQLException {
        CodedEnumTypeHandler handler = new CodedEnumTypeHandler(EnabledStatus.class);

        EasyMock.expect(rs.getInt(1)).andReturn(1).once();

        replayAll();
        EnabledStatus status = (EnabledStatus)handler.getNullableResult(rs,1);
        verifyAll();

        Assert.assertNotNull(status);
        Assert.assertEquals(EnabledStatus.enabled,status);
    }
}
