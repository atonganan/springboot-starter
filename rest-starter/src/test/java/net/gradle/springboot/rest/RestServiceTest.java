package net.gradle.springboot.rest;

import net.gradle.commons.pojo.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=TestApplication.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RestServiceTest {
    @Autowired private TestRestService service;

    @Test
    public void testSimpleReturnType(){
        Response<String> rest = service.testSimple();
        Assert.assertEquals(rest.data,"Hello");
    }

    @Test
    public void testNestedReturnType(){
        Response<Nested> rest = service.testNested();
        Assert.assertEquals(rest.data.code,10);
    }


    @Test
    public void testListReturnType(){
        Response<List<String>> rest = service.testList();
        Assert.assertEquals(2, CollectionUtils.size(rest.data));
    }

    @Test
    public void testExtendsReturnType(){
        NestedChild rest = service.testExtends(NestedChild.class);
        Assert.assertEquals("world",rest.property);
    }
}
