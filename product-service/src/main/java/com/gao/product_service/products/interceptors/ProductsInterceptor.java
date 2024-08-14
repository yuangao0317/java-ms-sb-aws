package com.gao.product_service.products.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/*
The ThreadContext class in the Log4j 2 library is used for managing per-thread context data
that can be included in log messages. This is especially useful for tracking context-specific information
like user IDs, session IDs, or request IDs across different threads in a multi-threaded application.
*/
@Component
public class ProductsInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        ThreadContext.put("requestId", request.getHeader("requestId"));
        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) throws Exception {

        ThreadContext.clearAll();
    }

}
