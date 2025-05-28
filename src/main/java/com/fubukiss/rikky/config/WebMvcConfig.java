package com.fubukiss.rikky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>Project: rikky-takeaway - WebMvcConfig 用于静态资源映射
 * <p>Powered by Riverify On 12-14-2022 23:52:20
 *
 * @author Riverify
 * @version 1.0
 * @since JDK8
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 设置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 用于设置静态资源映射
        log.info("开始进行静态资源映射...");
        long l = System.currentTimeMillis();
        
        // 配置图标文件的缓存控制
        registry.addResourceHandler("/front/images/favico.ico")
                .addResourceLocations("classpath:/front/images/")
                .setCacheControl(CacheControl.noCache());
                
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
        
        log.info("静态资源映射完毕 [用时" + (System.currentTimeMillis() - l) + "ms]");
    }

    /**
     * 扩展mvc框架的消息转换器
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        // 创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new ObjectMapper());
        // 将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0, messageConverter);
        log.info("自定义消息转换器已经添加到消息转换器列表中");
    }
}
