package net.gradle.springboot.mybatis.handlers;

import org.easymock.EasyMock;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonTypeHandlerTest extends EasyMockSupport {
    @Rule public EasyMockRule mocks = new EasyMockRule(this);
    @Mock private ResultSet rs;

    @Test
    public void testHandler() throws SQLException {
        JsonTypeHandler handler = new JsonTypeHandler(TestClass.class);
        EasyMock.expect(rs.getString(1)).andReturn("{\"configs\":{\"key1\":\"hello\",\"key2\":10}}").once();
        replayAll();
        TestClass result = (TestClass) handler.getNullableResult(rs,1);
        verifyAll();

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.configs);
        Assert.assertEquals("hello",result.configs.key1);
        Assert.assertEquals(10,result.configs.key2);
    }

    public static class TestClass{
        public Config configs;
        public static class Config{
            public String key1;
            public int key2;
        }
    }
}
