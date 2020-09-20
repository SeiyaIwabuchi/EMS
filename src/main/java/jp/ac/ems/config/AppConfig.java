package jp.ac.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * アプリケーション設定(config for application.)
 * @author user01-m
 *
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

	/**
	 * メッセージソース
	 */
    @Autowired
    private MessageSource messageSource;

    /**
     * 独自のバリデータを取得する
     */
    @Override
	public Validator getValidator() {
		return validator();
	}

    /**
     * 独自のバリデータを返す
     * @return バリデータ
     */
    @Bean
    public LocalValidatorFactoryBean validator()
    {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setValidationMessageSource(messageSource);
        return localValidatorFactoryBean;
    }
}