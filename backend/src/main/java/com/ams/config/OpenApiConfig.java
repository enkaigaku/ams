package com.ams.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("勤怠管理システム API")
                        .description("日本の企業向け勤怠管理システムのREST API\n\n" +
                                   "## 主な機能\n" +
                                   "- 出退勤打刻\n" +
                                   "- 休憩時間管理\n" +
                                   "- 休暇申請・承認\n" +
                                   "- 勤務時間修正申請\n" +
                                   "- 管理者ダッシュボード\n" +
                                   "- CSVエクスポート\n" +
                                   "- アラート・通知管理\n\n" +
                                   "## 認証\n" +
                                   "JWT（JSON Web Token）を使用した認証システムです。\n" +
                                   "1. `/auth/login` エンドポイントでログイン\n" +
                                   "2. レスポンスで受け取ったアクセストークンを使用\n" +
                                   "3. Authorization ヘッダーに `Bearer {token}` 形式で設定")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("開発チーム")
                                .email("dev@company.com")
                                .url("https://company.com"))
                        .license(new License()
                                .name("Company License")
                                .url("https://company.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("開発環境"),
                        new Server()
                                .url("https://api.company.com")
                                .description("本番環境")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWTアクセストークンを使用した認証\n\n" +
                                           "例: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")));
    }
}