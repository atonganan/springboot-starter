package net.gradle.springboot.easemob.entities;

/**
 * <p>
 * <p>
 * </p>
 *
 * @author lxt
 * @version 1.0
 * @Date 2017年2月23日
 */
public class MessageBody implements java.io.Serializable{
    public enum Message{
        //请求认证
        request((String)"您的认证申请已提交,请等待审核"),
        //认证完成
        finish((String)"您的认证申请已通过,开始接单吧"),
        //已支付／派单中
        paid((String)"支付成功"),

        //提现申请
        requestDraw((String)"您的提现申请已提交审核"),
        //提现拒绝
        refuse((String)"您的提现申请被拒绝,如有问题,请联系客服咨询"),
        //订单取消
        canceled((String)"您的订单已被取消,正在等待接单,请稍等"),
        //订单创建成功
        orderSuccess((String)"订单创建成功"),
        //订单已经完成
        orderFinish((String)"订单已完成"),
        //订单已接受
        orderReceive((String)"已接单");

        public String content;
        Message(String content){this.content=content;}
    }
}
