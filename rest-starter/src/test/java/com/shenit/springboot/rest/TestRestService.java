package com.shenit.springboot.rest;

import com.shenit.commons.pojo.Response;
import com.shenit.springboot.rest.annotations.Header;
import com.shenit.springboot.rest.annotations.Headers;
import com.shenit.springboot.rest.annotations.RestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@RestClient("http://localhost:8080")
public interface TestRestService {
    @GET
    @Path("/simple")
    Response<String> testSimple();

    @GET
    @Path("/nested")
    Response<Nested> testNested();

    @GET
    @Path("/list")
    Response<List<String>> testList();

    @GET
    @Path("/extends")
    <T extends Nested> T testExtends(Class<T> type);

    @GET @Path("/headers")
    @Headers({
            @Header(name="Test-Header1",value="abc"),
            @Header(name="Test-Header2",value="bcd")
    })
    void testHeaders();
}
