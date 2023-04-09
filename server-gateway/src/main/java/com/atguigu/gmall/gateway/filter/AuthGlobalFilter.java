package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;

import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.atguigu.gmall.common.util.AuthContextHolder.getUserId;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/4/8 20:05 周六
 * description:
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;


//    匹配路径工具类
    private AntPathMatcher antPathMatcher =new AntPathMatcher();

    @Value("${authUrls.url}")
        private String authUrls;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String path = uri.getPath();
        System.out.println("path:"+path);
//       获得访问的请求地址
        if (antPathMatcher.match("/**/inner/**",path)){
//           如果是内部接口，则网关拦截不允许外部访问！
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }

        String userId  = getUserId(request);
//token被盗用，根据getUserId方法，如果token被盗用了。就会返回-1
        if ("-1".equals(userId)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response,ResultCodeEnum.PERMISSION);
        }

        // 用户登录认证
        //api接口，异步请求，校验用户必须登录
        if (antPathMatcher.match("/api/**/auth/**",path)){
            if (StringUtils.isEmpty(userId)){
                ServerHttpResponse response = exchange.getResponse();
                return  out(response,ResultCodeEnum.LOGIN_AUTH);
            }
//
        }

        //            验证url
        for (String authUrl : authUrls.split(",")){
            // 当前的url包含登录的控制器域名，但是用户Id 为空！
            if (path.indexOf(authUrl)!=-1 && StringUtils.isEmpty(userId)){
                ServerHttpResponse response = exchange.getResponse();
                //303状态码表示由于请求对应的资源存在着另一个URI，应使用重定向获取请求的资源
                response.setStatusCode(HttpStatus.SEE_OTHER);
//                    重定向
                response.getHeaders().set(HttpHeaders.LOCATION,"http://www.gmall.com/login.html?originUrl="+request.getURI());
//                    重定向到登录
                return response.setComplete();
            }

        }
//        将userid传到后端
        if (!StringUtils.isEmpty(userId)){
            request.mutate().header("userId",userId).build();
//                将request变成exchange对象
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }

    private String getUserId(ServerHttpRequest request) {
        String token="";
        List<String> tokenList = request.getHeaders().get("token");
        if (CollectionUtils.isEmpty(tokenList)){
            token = tokenList.get(0);
        }else {
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("token");
            if (cookie!=null){
                //根据编码进行解码。
               token=URLDecoder.decode(cookie.getValue());
            }
        }

        if (StringUtils.isEmpty(token)){
            String userString = (String) redisTemplate.opsForValue().get("user:login:" + token);
            JSONObject jsonObject = JSONObject.parseObject(userString);
            String ip = jsonObject.getString("ip");
            String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
//            验证token是否被调用；
            if (gatwayIpAddress.equals(ip)){
                return jsonObject.getString("userId");
            }else {
//                ip不一致。被盗用了。
                return "-1";
            }
        }
        return "";
    }


    /**
     * @Date :2023/4/8 20:32
     * @param :[response, permission]
     * @return :reactor.core.publisher.Mono<java.lang.Void>
     * @description :接口鉴权失败返回数据
     * @author :clarence
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
//  返回用户没有权限登录
        Result<Object> build = Result.build(null, resultCodeEnum);
        byte[] bytes = JSONObject.toJSONString(build).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
//        输入到页面上
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * @Date :2023/4/9 12:28
     * @param :[request]
     * @return :java.lang.String
     * @description :网关中获取临时用户id
     * @author :clarence
     */
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId="";
        List<String> userTempIdList = request.getHeaders().get("UserTempId");
        if (CollectionUtils.isEmpty(userTempIdList)){
            userTempId=userTempIdList.get(0);
        }else {
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            HttpCookie cookie = cookies.getFirst("UserTempId");
            if (cookie!=null){
                userTempId = URLDecoder.decode(cookie.getValue());
            }
        }
        return userTempId;
    }

}
