package pl.matisoft.soy.example;

import com.google.common.collect.Lists;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import pl.matisoft.soy.ajax.config.SpringSoyViewAjaxConfig;

/**
 * Created with IntelliJ IDEA.
 * User: mati
 * Date: 27/06/2013
 * Time: 23:02
 */
@Configuration
@Import(SpringSoyViewAjaxConfig.class)
@PropertySource("classpath:spring-soy-view-example.properties")
@EnableWebMvc
@ComponentScan(basePackages = {"pl.matisoft.soy.example"})
public class AppConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public HttpAsyncClient createHttpAsyncClient() {
        final HttpComponentsAsyncClientHttpRequestFactory httpComponentsAsyncClientHttpRequestFactory = new HttpComponentsAsyncClientHttpRequestFactory();
        httpComponentsAsyncClientHttpRequestFactory.afterPropertiesSet();

        return httpComponentsAsyncClientHttpRequestFactory.getHttpAsyncClient();
    }

    @Bean
    @Primary
    public ViewResolver contentNegotiatingViewResolver(final ViewResolver soyViewResolver) throws Exception {
        final ContentNegotiatingViewResolver contentNegotiatingViewResolver = new ContentNegotiatingViewResolver();
        contentNegotiatingViewResolver.setViewResolvers(Lists.newArrayList(soyViewResolver));
        contentNegotiatingViewResolver.setDefaultViews(Lists.<View>newArrayList(new MappingJackson2JsonView()));

        return contentNegotiatingViewResolver;
    }

}
