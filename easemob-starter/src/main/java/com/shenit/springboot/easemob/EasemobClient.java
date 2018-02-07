package com.shenit.springboot.easemob;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.shenit.commons.utils.GsonUtils;
import com.shenit.commons.utils.ShenStrings;
import com.shenit.commons.utils.Payload;
import com.shenit.springboot.easemob.entities.EasemobRoom;
import com.shenit.springboot.easemob.entities.EasemobUser;
import com.shenit.springboot.easemob.forms.UserForm;
import com.shenit.springboot.easemob.requests.TokenRequest;
import com.shenit.springboot.easemob.responses.EasemobResponse;
import com.shenit.springboot.easemob.responses.TokenResponse;
import com.shenit.springboot.easemob.requests.SysMessageRequest;
import com.shenit.springboot.easemob.responses.EasemobDataResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 环讯客户端.
 * Created by jiangnan on 17/06/2017.
 */
public class EasemobClient {
    private static final Logger LOG = LoggerFactory.getLogger(EasemobClient.class);
    private static final String AUTH_PREFIX = "Bearer ";
    private final TokenRequest tokenRequest;
    private static final List<HttpMessageConverter<?>> CONVERTERS = Lists.newArrayList();
    static{
        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        gsonConverter.setGson(GsonUtils.inst());
        CONVERTERS.add(gsonConverter);
    }

    public final String appName;
    public final String clientId;
    public final String appKey;
    public final String clientSecret;
    public final String baseUrl;
    public final String orgName;

    private String token;

    private RestTemplate restTmpl;
    private ScheduledFuture<?> tokenTask;



    public EasemobClient(String host, String clientId, String appKey, String clientSecret){
        this.clientId = clientId;
        this.appKey = appKey;
        this.clientSecret = clientSecret;

        String[] pair = appKey.split("#");
        this.orgName = pair[0];
        this.appName = pair[1];
        this.baseUrl = ShenStrings.joinWith("https:/",host,orgName,appName,"/");
        if (LOG.isDebugEnabled()) LOG.debug("[EasemobClient([host, appid, appkey, secret])] baseUrl -> {}", baseUrl);
        restTmpl = new RestTemplate(CONVERTERS);
        restTmpl.setErrorHandler(new ResponseErrorHandler() {
                 @Override
                 public boolean hasError(ClientHttpResponse response) throws IOException {
                     return !response.getStatusCode().is2xxSuccessful();
                 }

                 @Override
                 public void handleError(ClientHttpResponse response) throws IOException {
                    //do nothing
                     LOG.warn("[handleError([response])] Send request to easemob throws exception, response -> [{}] {}", response.getStatusCode(), response.getStatusText());
                 }
             }

        );

        tokenRequest = new TokenRequest(clientId, clientSecret);

        getToken(true);

    }

    public synchronized String getToken(boolean forceReload){

        //如果缓存有，先从缓存拿
        if(!forceReload && StringUtils.isNotBlank(token)) return token;
        String url = url("/token");
        //获取token信息
        ResponseEntity<TokenResponse> entity = restTmpl.exchange(
                url,
                HttpMethod.POST,
                entity(tokenRequest, false),
                TokenResponse.class);
        if(!HttpStatus.OK.equals(entity.getStatusCode())){
            if (LOG.isDebugEnabled()) LOG.debug("[getToken([forceReload])] call url -> {} failed, response -> {}",url, entity );
            return null;
        }
        TokenResponse resp = entity.getBody();
        //响应没内容
        if(resp == null) return null;
        this.token = resp.accessToken;

        if(tokenTask != null && !tokenTask.isDone()) tokenTask.cancel(true);

        tokenTask = Executors.newSingleThreadScheduledExecutor()
                .schedule(() -> {
                    token = getToken(true);
                },resp.expireSeconds,TimeUnit.SECONDS);


        if (LOG.isTraceEnabled()) LOG.trace("[getToken([forceReload])] access token acquired -> {}", GsonUtils.format(resp));
        return resp.accessToken;
    }

    /**
     * 创建用户接口.
     * @param users 用户账号
     * @return
     */
    public List<EasemobUser> createUser(UserForm... users){
        String url = url("/users");
        if(ArrayUtils.isEmpty(users)) throw new IllegalArgumentException("缺少注册用户信息");
        HttpEntity request = null;
        if(users.length == 1) {
            //只有一个的时候，用这个接口
            request = entity(users[0], true);
        }else{
            request = entity(users,true);
        }
        ResponseEntity<String> response =  restTmpl.exchange(url,
                HttpMethod.POST,
                request,
                String.class);
        if (LOG.isDebugEnabled()) LOG.debug("[createUser([username,password,nick])] url -> {}, entity -> {}",url, GsonUtils.format(request));
        if(!HttpStatus.OK.equals(response.getStatusCode())){
            if (LOG.isDebugEnabled()) LOG.debug("[createUser([username, password, nick])] Calling url -> {} not success -> {}, {}",
                    url ,response.toString(),response.getBody());
            return null;
        }
        EasemobResponse<EasemobUser> data = GsonUtils.parse(response.getBody(),
                new TypeToken<EasemobResponse<EasemobUser>>(){});
        return data.entities;
    }


    /**
     * 获取房间信息接口.
     * @param roomId 房间号
     * @return
     */
    public EasemobRoom getChatRoomInfo(String roomId){
        String url = url("/chatrooms/"+roomId);
        ResponseEntity<String> entity =  restTmpl.exchange(url,
                HttpMethod.GET,
                entity(null,true),
                String.class);
        if (LOG.isDebugEnabled()) LOG.debug("[getChatRoomInfo([roomId])] url -> {}, entity -> {}",url, GsonUtils.format(entity));
        if(!HttpStatus.OK.equals(entity.getStatusCode())){
            if (LOG.isDebugEnabled()) LOG.debug("[getChatRoomInfo([roomId])] Calling url -> {} not success -> {}, {}",
                    url ,entity.toString(),entity.getBody());
            return null;
        }
        EasemobDataResponse<EasemobRoom> data = GsonUtils.parse(entity.getBody(),new TypeToken<EasemobDataResponse<EasemobRoom>>(){});
        return data.data(0);
    }

    /**
     * 获取用户加入的房间猎豹
     * @param username 用户账号
     * @return
     */
    public List<EasemobRoom> joinedRooms(String username){
        String url = url("/users/"+username+"/joined_chatrooms");
        ResponseEntity<String> entity =  restTmpl.exchange(url,
                HttpMethod.GET,
                entity(null,true),
                String.class);
        if (LOG.isDebugEnabled()) LOG.debug("[joinedRooms([roomId])] url -> {}, entity -> {}",url, GsonUtils.format(entity));
        if(!HttpStatus.OK.equals(entity.getStatusCode())){
            if (LOG.isDebugEnabled()) LOG.debug("[joinedRooms([roomId])] Calling url -> {} not success -> {}, {}",
                    url ,entity.toString(),entity.getBody());
            return null;
        }
        EasemobDataResponse<EasemobRoom> data = GsonUtils.parse(entity.getBody(),new TypeToken<EasemobDataResponse<EasemobRoom>>(){});
        return data.data;
    }

    /**
     * 删除用户接口.
     * @param username 用户账号
     * @return
     */
    public EasemobUser getUser(String username){
        String url = url("/users/"+username);
        ResponseEntity<String> entity =  restTmpl.exchange(url,
                HttpMethod.GET,
                entity(null,true),
                String.class);
        if (LOG.isDebugEnabled()) LOG.debug("[getUser([username])] url -> {}, entity -> {}",url, GsonUtils.format(entity));
        if(!HttpStatus.OK.equals(entity.getStatusCode())){
            if (LOG.isDebugEnabled()) LOG.debug("[deleteUser([username])] Calling url -> {} not success -> {},{}",
                    url ,entity.toString(),entity.getBody());
            return null;
        }
        EasemobResponse<EasemobUser> data = GsonUtils.parse(entity.getBody(),new TypeToken<EasemobResponse<EasemobUser>>(){});
        return data.first();
    }


    /**
     * 删除用户接口.
     * @param username 用户账号
     * @return
     */
    public boolean deleteUser(String username){
        String url = url("/users/"+username);
        ResponseEntity<String> entity =  restTmpl.exchange(url,
                HttpMethod.DELETE,
                entity(null,true),
                String.class);
        if (LOG.isDebugEnabled()) LOG.debug("[deleteUser([username])] url -> {}, entity -> {}",url, GsonUtils.format(entity));
        if(!HttpStatus.OK.equals(entity.getStatusCode())){
            if (LOG.isDebugEnabled()) LOG.debug("[deleteUser([username])] Calling url -> {} not success -> {},{}",
                    url ,entity.toString(),entity.getBody());
            return false;
        }
        return true;
    }


    /**
     * 创建聊天室
     * @param title
     * @param ownerId
     * @return
     */
    public String createChatRoom(String title,String ownerId){
        String url = url("/chatrooms");
        ResponseEntity<String> entity =  restTmpl.exchange(url,
                HttpMethod.POST,
                entity(Payload.wrap(
                        "name",title,
                        "description",title,
                        "owner",ownerId),true),
                String.class);
        if (LOG.isDebugEnabled()) LOG.debug("[createChatRoom([title, ownerId])] url -> {}, entity -> {}",url, GsonUtils.format(entity));
        if(!HttpStatus.OK.equals(entity.getStatusCode())){
            if (LOG.isDebugEnabled()) LOG.debug("[createChatRoom([title, ownerId])] Create chat room not ok. url -> {}, resp -> {},{}",
                    url, entity.getStatusCodeValue(),entity.getBody());
            return null;
        }
        EasemobResponse<Void> resp = GsonUtils.parse(entity.getBody(),new TypeToken<EasemobResponse<Void>>(){});
        return ShenStrings.str(resp.getData("id"));
    }

    /**
     * 删除聊天室
     * @param roomId 聊天室ID
     * @return
     */
    public boolean deleteChatRoom(String roomId){
        String url = url("/chatrooms/"+roomId);
        ResponseEntity<String> entity =  restTmpl.exchange(url,
                HttpMethod.DELETE, entity(null,true),
                new ParameterizedTypeReference<String>(){});
        if (LOG.isDebugEnabled()) LOG.debug("[deleteChatRoom([roomId])] Calling url -> {}, entity -> {]",url, GsonUtils.format(entity));
        if(!HttpStatus.OK.equals(entity.getStatusCode())){
            if (LOG.isDebugEnabled()) LOG.debug("[deleteChatRoom([rooId])] Could not delete chat room [{}] -> {},{}",
                    roomId, entity.getStatusCodeValue(),entity.getBody());
            return false;
        }
        EasemobResponse<Void> resp = GsonUtils.parse(entity.getBody(),new TypeToken<EasemobResponse<Void>>(){});
        return (Boolean)resp.getData("success");
    }

    /**
     * 尝试登出用户
     * @param uid
     * @return
     */
    public String getUserStatus(String uid) {
        String url = url("/users/"+uid+"/status");
        ResponseEntity<String> entity =  restTmpl.exchange(url,
                HttpMethod.GET, entity(null,true),
                new ParameterizedTypeReference<String>(){});
        if (LOG.isDebugEnabled()) LOG.debug("[logoutUser([roomId])] Calling url -> {}",url);
        if(!HttpStatus.OK.equals(entity.getStatusCode())) return null;
        EasemobResponse<Void> resp = GsonUtils.parse(entity.getBody(),new TypeToken<EasemobResponse<Void>>(){});
        return (String)resp.getData("stliu");
    }

    /**
     * 发送Action信息给用户端，这个信息只会给程序使用.
     * @param action Action消息
     * @param uid UID
     * @param params 额外参数
     */
    public void sendActionToUser(String action,long uid, Map<String,Object> params ){
        //TODO to be implement

    }

    /**
     * 发送系统消息给用户，这个信息会出现在用户的系统消息界面.
     * @param msg 消息内容
     * @param uids
     * @param
     */
    public EasemobResponse<EasemobUser> sendSysMessageToUsers(String msg,Long[] uids,Payload payload,String from){
        try {
            String url = url("/messages");
            ResponseEntity<String> entity = restTmpl.exchange(url,
                    HttpMethod.POST,
                    entity(new SysMessageRequest(uids, msg, payload, from), true),
                    String.class);
            if (!HttpStatus.OK.equals(entity.getStatusCode())) {
                if (LOG.isDebugEnabled())
                    LOG.debug("[sendSysMessageToUsers([msg, uids, params])] Calling url -> {} not success -> {},{}",
                            url, entity.toString(), entity.getBody());
                return null;
            }
            return  GsonUtils.parse(entity.getBody(),new TypeToken<EasemobResponse<EasemobUser>>(){});
        }catch(Exception ex){
            if (LOG.isDebugEnabled()) LOG.debug("[sendSysMessageToUsers([msg, uids, payload, from])] send easmob message failed", ex);
        }
        return null;
    }



    /**
     * Encrypt entity
     * @param payload 请求参数
     * @return
     */
    private HttpEntity<?> entity(Object payload, boolean auth) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
        if(auth) headers.set(HttpHeaders.AUTHORIZATION,AUTH_PREFIX+getToken(false));
        return new HttpEntity<>(payload,headers);
    }


    /**
     * 生成请求URL
     * @param path
     * @return
     */
    private String url(String path){
        return baseUrl + path;
    }

    public void setRequestFactory(ClientHttpRequestFactory factory){
        restTmpl.setRequestFactory(factory);
    }

    protected RestTemplate getRestTemplate() {
        return restTmpl;
    }

    protected void setRestTemplate(RestTemplate  tmpl){
        this.restTmpl = tmpl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
