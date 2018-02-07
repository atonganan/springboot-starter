package com.shenit.springboot.rest.invokers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.shenit.commons.utils.*;
import com.shenit.springboot.rest.annotations.Header;
import com.shenit.springboot.rest.annotations.Headers;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 最基础的REST服务调用实现类.
 */
public class BasicRestInvoker implements RestInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(BasicRestInvoker.class);
    private static final String VOID = "void";
    private HttpHeaders headerMap;
    private Map<Integer,String> headerParamMap;
    private int bodyPos;
    private Map<Integer,String> queryParamMap;
    private Map<Integer,String> pathParamMap;
    private Method method;
    private int[] genericTypePos;
    private Type returnType;
    private String url;
    private HttpMethod httpMethod;
    private RestTemplate restTemplate;

    public void setRestTemplate(RestTemplate rest) {
        this.restTemplate = rest;
    }

    /**
     * 构造一个基础的REST调用类.
     * @param method
     */
    public BasicRestInvoker(Method method,String baseUrl) {
        if(StringUtils.isEmpty(baseUrl))
            throw new IllegalArgumentException("baseUrl should not be null!!");
        Annotation[] annotations = method.getAnnotations();
        url = loadUrl(findAnnotation(annotations,Path.class),baseUrl);

        returnType = determineReturnType(method);

        //加载当前类全局作用header
        headerMap = loadGlobalHeaders(method);

        //决定执行方法.
        httpMethod = loadHttpMethod(annotations);

        registerParameters(method);
    }

    /**
     * 查好方法的类型
     * @param method
     * @return
     */
    private Type determineReturnType(Method method) {
        //尝试检查方法里面是否含有指定最终返回泛型类型的参数
        Type[] paramTypes = method.getGenericParameterTypes();
        List<Integer> pos = Lists.newArrayList();
        for(int i=0;i<paramTypes.length;i++){
            //记录这些参数的位置
            if(paramTypes[i] instanceof ParameterizedType && ((ParameterizedType)paramTypes[i]).getRawType().equals(Class.class))
                pos.add(i);
        }
        this.genericTypePos = pos.isEmpty() ? null  : ArrayUtils.toPrimitive(pos.toArray(new Integer[0]));
        //如果没有返回通用的返回类型
        return method.getGenericReturnType();
    }

    /**
     * 加载Body参数配置
     * @param method
     * @return
     */
    private void registerParameters(Method method) {

        Parameter[] params = method.getParameters();
        for(int i=0;i < params.length;i++){
            Parameter param = params[i];
            Annotation[] annotations = param.getAnnotations();
            HeaderParam headerParam = findAnnotation(annotations,HeaderParam.class);
            //两者只能取一，如果都有定义，则取JAX-RS的版本
            if(headerParam != null){
                registerHeaderParam(i,headerParam.value());
                continue;
            }

            RequestHeader requestHeaderParam = findAnnotation(annotations,RequestHeader.class);
            if(requestHeaderParam != null){
                registerHeaderParam(i, ShenValidates.anyNotBlank(requestHeaderParam.name(),requestHeaderParam.value()));
                continue;
            }

            QueryParam queryParam = findAnnotation(annotations,QueryParam.class);
            if(queryParam != null){
                registerQueryParam(i,queryParam.value());
                continue;
            }

            RequestParam requestParam = findAnnotation(annotations,RequestParam.class);
            if(requestParam != null){
                registerQueryParam(i, ShenValidates.anyNotBlank(requestParam.name(),requestParam.value()));
                continue;
            }

            PathParam pathParam = findAnnotation(annotations,PathParam.class);
            if(pathParam != null){
                registerPathParam(i,pathParam.value());
                continue;
            }

            PathVariable pathVar = findAnnotation(annotations,PathVariable.class);
            if(pathVar != null){
                registerPathParam(i, ShenValidates.anyNotBlank(pathVar.name(),pathVar.value()));
                continue;
            }

            //记录最后一个RequestBody的位置
            RequestBody body = findAnnotation(annotations,RequestBody.class);
            if(body != null){
                bodyPos = i;
            }
        }
    }

    /**
     * 加载HTTP方法
     * @param annotations
     * @return
     */
    private HttpMethod loadHttpMethod(Annotation[] annotations) {
        httpMethod = HttpMethod.GET;
        for(Annotation a : annotations) {
            Class<?> type= a.annotationType();
            if(type.equals(POST.class)){
                httpMethod = HttpMethod.POST;
                break;
            }else if(type.equals(DELETE.class)){
                httpMethod = HttpMethod.DELETE;
                break;
            }
        }

        return httpMethod;
    }

    /**
     * 加载全局Header
     * @param method
     * @return
     */
    private HttpHeaders loadGlobalHeaders(Method method) {
        //先加载全局的
        Set<Header> headers = loadDefaultHeaders(method.getDeclaringClass().getAnnotations());
        //然后加载方法级别的
        headers.addAll(loadDefaultHeaders(method.getAnnotations()));
        //没有请求头需要处理.
        if(CollectionUtils.isEmpty(headers)) return null;
        headerMap = new HttpHeaders();
        for(Header header : headers) headerMap.add(header.name(),header.value());
        return headerMap;
    }

    /**
     * 加载默认的Header
     * @param annotations
     * @return
     */
    private Set<Header> loadDefaultHeaders(Annotation[] annotations) {
        Set<Header> headers = findAnnotations(annotations,Header.class);
        Headers headersAnnotation = findAnnotation(annotations,Headers.class);
        if(headersAnnotation != null){
            //对于多个头的特殊处理
            for(Header header : headersAnnotation.value()) headers.add(header);
        }
        return headers;
    }

    /**
     * 加载URL
     * @param path Path annotation
     * @param baseUrl
     * @return
     */
    private String loadUrl(Path path, String baseUrl) {
        String pathVal = StringUtils.isEmpty(path.value()) ? ShenStrings.SLASH : path.value();
        if(StringUtils.isNotEmpty(pathVal))
            pathVal = pathVal.startsWith(ShenStrings.SLASH) ? pathVal : ShenStrings.SLASH + pathVal;

        String url = baseUrl + pathVal;
        if (LOG.isTraceEnabled()) LOG.trace("[loadUrl([method, baseUrl])] Path -> {} for method -> {}",url, method.toString() );
        return url;
    }

    /**
     * 注册头部信息
     * @param pos
     * @param header
     */
    private void registerHeaderParam(int pos, String header) {
        if(headerParamMap == null) headerParamMap = Maps.newHashMap();
        headerParamMap.put(pos,header);
    }

    /**
     * 注册请求参数
     * @param pos
     * @param name
     */
    private void registerQueryParam(int pos, String name) {
        if(queryParamMap == null) queryParamMap = Maps.newHashMap();
        queryParamMap.put(pos,name);
    }

    /**
     * 注册路径参数
     * @param pos
     * @param name
     */
    private void registerPathParam(int pos, String name) {
        if(pathParamMap == null) pathParamMap = Maps.newHashMap();
        pathParamMap.put(pos,name);
    }


    @Override
    public Object invoke(Object[] args) {
        HttpEntity request = createRequest(args);
        String requestUrl = getServiceUrl(args);
        Map<String,Object> pathParams = loadParams(pathParamMap,args);
        ResponseEntity<String> resp = MapUtils.isNotEmpty(pathParamMap) ?
                restTemplate.exchange(requestUrl, httpMethod, request, String.class, pathParams) :
                restTemplate.exchange(requestUrl, httpMethod, request, String.class) ;
        if (LOG.isTraceEnabled()) LOG.trace("[invoke([args])] calling -> {} {} successfully", httpMethod, requestUrl);
        String body = resp.getBody();
        return body.equals(VOID) ? null : GsonUtils.parse(body,findReturnType(args));
    }

    /**
     * 根据参数获取返回类型
     * @param args
     * @return
     */
    private Type findReturnType(Object[] args) {
        if(genericTypePos != null){
            //对于泛型，根据参数中的泛型类型决定最终返回类型
            Type[] types = new Type[genericTypePos.length];
            for(int i=0;i<genericTypePos.length;i++) types[i] = (Type) args[genericTypePos[i]];
            return TypeToken.getParameterized(returnType, types).getType();
        }

        return returnType;
    }


    /**
     * 创建请求
     * @param args
     * @return
     */
    private HttpEntity createRequest(Object[] args) {
        HttpHeaders headers = loadHeaders(args);
        //注册body
        Object body = httpMethod.equals(HttpMethod.POST) ? ShenArrays.get(args,bodyPos) : null;
        return new HttpEntity(body,headers);
    }

    /**
     * 加载请求头信息
     * @param args
     * @return
     */
    private HttpHeaders loadHeaders(Object[] args) {
        HttpHeaders headers = new HttpHeaders();
        //加载定义在方法级别的请求头
        if(MapUtils.isNotEmpty(headerMap)) headers.putAll(headerMap);
        if(MapUtils.isEmpty(headerParamMap)) return headers;

        for(int i : headerParamMap.keySet()){
            headers.add(headerParamMap.get(i), ShenStrings.str(ShenArrays.get(args,i,StringUtils.EMPTY)));
        }
        return headers;
    }

    /**
     * 加载请求体
     * @param paramMap 参数配置表
     * @param args
     * @return
     */
    private Payload loadParams(Map<Integer,String> paramMap,Object[] args) {
        if(MapUtils.isEmpty(paramMap)) return null;

        Payload params = new Payload();
        for (int i : paramMap.keySet()) {
            params.put(paramMap.get(i), ShenArrays.get(args, i));
        }
        return params;
    }

    /**
     * 生成服务地址
     * @return
     * @param args
     */
    protected String getServiceUrl(Object[] args) {
        Payload params = loadParams(queryParamMap,args);
        return MapUtils.isEmpty(params)? url : url + ShenStrings.QUESTION_MARK + params.toQuery();
    }

    /**
     * 查找注解
     * @param annotations
     * @param type
     * @param <T>
     * @return
     */
    protected <T> T findAnnotation(Annotation[] annotations, Class<T> type){
        for(Annotation annotation : annotations){
            if(annotation.annotationType().equals(type)) return (T) annotation;
        }
        return null;
    }
    /**
     * 查找注解
     * @param annotations
     * @param type
     * @param <T>
     * @return
     */
    protected <T> Set<T> findAnnotations(Annotation[] annotations, Class<T> type) {
        Set<T> targets = Sets.newLinkedHashSet();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(type)) targets.add((T) annotation);
        }
        return targets;
    }


}
