/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.documentation;

import com.mangofactory.swagger.paths.SwaggerPathProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

/**
 * Configuration class for Swagger library.
 * @since 17. 08. 2015
 * Target URL format: http://localhost:8888/repository/api-docs
 */
@Configuration
@EnableSwagger
public class SwaggerConfig {
    private static final String API_VERSION = "1.0";
    private static final String URL_BASE_PATH = "repository";

    private SpringSwaggerConfig springSwaggerConfig;

    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }

    @Bean
    public SwaggerSpringMvcPlugin customImplementation() {
        SwaggerPathProvider pathProvider = new PathProvider();
        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
                .apiInfo(apiInfo())
                .pathProvider(pathProvider)
                .apiVersion(SwaggerConfig.API_VERSION)
                .swaggerGroup(SwaggerConfig.URL_BASE_PATH);
    }

    private ApiInfo apiInfo() {
        String title = "DataCleaner REST API";
        String description = title;
        String email = "";
        String licenceType = "GNU Lesser General Public License";
        String licenceURL = "http://www.gnu.org/licenses/lgpl-3.0.html";
        String termsOfService = licenceURL;

        return new ApiInfo(title, description, termsOfService, email, licenceType, licenceURL);
    }

    private static class PathProvider extends SwaggerPathProvider {
        public PathProvider() {
        }

        @Override
        protected String applicationPath() {
            return "/" + SwaggerConfig.URL_BASE_PATH;
        }

        @Override
        protected String getDocumentationPath() {
            return "/";
        }
    }
}
