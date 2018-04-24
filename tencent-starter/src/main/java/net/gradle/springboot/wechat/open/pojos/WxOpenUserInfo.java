package net.gradle.springboot.wechat.open.pojos;

import com.google.gson.annotations.SerializedName;
import net.gradle.commons.utils.ShenValidates;
import net.gradle.springboot.wechat.pojos.WechatResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * 微信开放平台用户信息.
 */
public class WxOpenUserInfo extends WechatResponse {
    private static final int MALE_VAL = 1;
    public String openid;
    public String unionid;
    public String nickname;
    public int sex;
    public String province;
    public String city;
    @SerializedName("headimgurl")
    public String avatar;
    public Set<String> privilege;

    /**
     * 判斷是否男性
     * @return
     */
    public boolean isMale(){
        return ShenValidates.eq(sex,MALE_VAL);
    }

    public String getUserId() {
        return StringUtils.isNotEmpty(unionid) ? unionid : openid;
    }
}
