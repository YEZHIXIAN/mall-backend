package com.zhixian.mall.cart.interceptor;

import com.zhixian.mall.cart.vo.UserInfoTo;
import com.zhixian.mall.common.constant.AuthServerConstant;
import com.zhixian.mall.common.constant.CartConstant;
import com.zhixian.mall.common.vo.MemberResponseVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在请求处理之前调用，将用户信息保存到UserInfoTo中
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @return 是否继续执行后续操作
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        // 如果用户已登录，将用户信息保存到UserInfoTo中
        if (member != null) {
            userInfoTo.setUserId(member.getId());
        }

        // 如果用户未登录，从cookie中获取临时用户信息
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName())) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }

        // 如果用户未登录，且cookie中没有临时用户信息，生成临时用户信息
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            // 生成临时用户信息
            String uuid = java.util.UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }

        // 保存到ThreadLocal中
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     *
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @param modelAndView 视图
     * @throws Exception 异常
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {


        if (threadLocal.get().isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, threadLocal.get().getUserKey());
            cookie.setDomain("mall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
