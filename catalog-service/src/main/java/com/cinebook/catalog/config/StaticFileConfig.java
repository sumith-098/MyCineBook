// package com.cinebook.catalog.config;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// /** Serves uploaded movie posters back out at /uploads/posters/<file>. */
// @Configuration
// public class StaticFileConfig implements WebMvcConfigurer {

//     @Value("${app.uploads.dir}")
//     private String uploadsDir;

//     @Override
//     public void addResourceHandlers(ResourceHandlerRegistry registry) {
//         registry.addResourceHandler("/uploads/**")
//                 .addResourceLocations("file:" + uploadsDir + "/");
//     }
// }
