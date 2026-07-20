package com.afei.common.constant;

public class RedisKey {
    /** Token 黑名单前缀 */
    public static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /** 用户登录信息前缀 */
    public static final String USER_LOGIN_PREFIX = "user:login:";

    /** 购物车前缀 */
    public static final String CART_PREFIX = "cart:";

    /** 商品分类树缓存 */
    public static final String CATEGORY_TREE = "product:category:tree";

    /** 验证码前缀 */
    public static final String CAPTCHA_PREFIX = "captcha:";
}
