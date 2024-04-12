package com.qfnu.reggie.config;

import com.qfnu.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport {

    /**
     * 扩展MVC消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展的消息转换器");

        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson把Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //把上面的消息转换器对象追加到mvc框架的转换器集合中，设置索引为0，表示优先使用自定义的转换器对象
        converters.add(0, messageConverter);
    }
}
