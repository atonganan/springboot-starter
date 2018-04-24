package net.gradle.springboot.rest;

import com.google.common.collect.Lists;
import net.gradle.commons.pojo.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestRest {
    @RequestMapping("/simple")
    public Response<String> testSimple(){
        return Response.data("Hello");
    }

    @RequestMapping("/nested")
    public Response<Nested> testNested(){
        return Response.data(new Nested("hello",10));
    }

    @RequestMapping("/list")
    public Response<List<String>> testList(){
        return Response.data(Lists.newArrayList("a","b"));
    }

    @RequestMapping("/extends")
    public NestedChild testExtends(){
        return new NestedChild("hello",1,"world");
    }
}
