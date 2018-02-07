package com.shenit.springboot.oauth.qq.pojos;

import com.google.gson.annotations.SerializedName;
import com.shenit.commons.utils.ShenValidates;

/**
 * QQ用户信息.
 */
public class QqUserInfo extends QqResponse {
    private static final String MALE_VAL = "男";
    public String nickname;
    @SerializedName("figureurl")
    public String avatar30x30;
    @SerializedName("figureurl_1")
    public String avatar50x50;
    @SerializedName("figureurl_2")
    public String avatar100x100;
    //QQ小头像
    @SerializedName("figureurl_qq_1")
    public String avatarQqSmall;
    //QQ大头像
    @SerializedName("figureurl_qq_2")
    public String avatarQqLarge;
    public String gender;
    //是否黄钻用户
    @SerializedName("is_yellow_vip")
    public boolean yellowVip;
    //是否黄钻年费用户
    @SerializedName("is_yellow_year_vip")
    public boolean yellowYearVip;
    //黄钻等级
    @SerializedName("yellow_vip_level")
    public int yellowVipLevel;
    //VIP
    @SerializedName("vip")
    public boolean vip;
    //等级
    public int level;



    /**
     * 判斷是否男性
     * @return
     */
    public boolean isMale(){
        return ShenValidates.eq(gender,MALE_VAL);
    }

}
