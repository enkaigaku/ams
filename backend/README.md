# AMS Backend - 打刻管理システム

Spring Boot 3を使用した勤怠管理システムのバックエンドAPIです。

## 技術スタック

- **Java 17+**
- **Spring Boot 3.2+**
- **PostgreSQL** (データベース)
- **Spring Security 6** (認証・認可)
- **JWT** (トークン認証)
- **Flyway** (データベースマイグレーション)
- **Gradle** (ビルドツール)
- **SpringDoc OpenAPI** (API ドキュメント)

## 主要機能

### 認証・認可
- JWT ベースの認証システム
- リフレッシュトークンによるトークン更新
- ロールベースアクセス制御 (従業員/管理者)

### 勤怠管理
- 出勤・退勤打刻
- 休憩時間管理
- 勤怠履歴の記録・照会
- 自動ステータス計算

### 申請・承認
- 有給休暇申請
- 打刻修正申請
- 管理者による承認・却下

### 管理機能
- チーム状況の把握
- アラート管理
- レポート生成・CSV エクスポート

## セットアップ

### 前提条件
- Java 17 以上
- PostgreSQL 12 以上
- Gradle 8.x

### データベース設定
```sql
CREATE DATABASE ams_db;
CREATE USER ams_user WITH PASSWORD 'ams_password';
GRANT ALL PRIVILEGES ON DATABASE ams_db TO ams_user;
```

### 環境変数
```bash
export DB_USERNAME=ams_user
export DB_PASSWORD=ams_password
export JWT_SECRET=your-secret-key-here
export CORS_ORIGINS=http://localhost:5173,http://localhost:3000
```

### 実行方法
```bash
# 依存関係のダウンロード
./gradlew build

# アプリケーション起動
./gradlew bootRun

# テスト実行
./gradlew test
```

## API エンドポイント

### 認証
- `POST /api/auth/login` - ログイン
- `POST /api/auth/refresh` - トークン更新
- `POST /api/auth/logout` - ログアウト
- `GET /api/auth/me` - 現在のユーザー情報

### 打刻
- `POST /api/time/clock-in` - 出勤打刻
- `POST /api/time/clock-out` - 退勤打刻
- `POST /api/time/break-start` - 休憩開始
- `POST /api/time/break-end` - 休憩終了
- `GET /api/time/today` - 本日の勤怠
- `GET /api/time/history` - 勤怠履歴

### 申請
- `POST /api/requests/leave` - 休暇申請
- `POST /api/requests/time-modification` - 打刻修正申請
- `GET /api/requests/leave` - 休暇申請一覧
- `GET /api/requests/time-modification` - 打刻修正申請一覧

### 管理者機能
- `GET /api/manager/team` - チーム状況
- `GET /api/manager/alerts` - アラート一覧
- `POST /api/requests/{id}/approve` - 申請承認
- `POST /api/requests/{id}/reject` - 申請却下
- `GET /api/manager/reports/csv` - CSV レポート

## データベース構造

### 主要テーブル
- `users` - ユーザー情報
- `departments` - 部署情報
- `time_records` - 勤怠記録
- `leave_requests` - 休暇申請
- `time_modification_requests` - 打刻修正申請
- `alerts` - アラート
- `refresh_tokens` - リフレッシュトークン

## 開発

### コード規約
- Java コーディング規約に準拠
- Spring Boot のベストプラクティスに従う
- すべてのパブリックメソッドにJavadocを記述

### テスト
- 単体テスト: JUnit 5 + Mockito
- 統合テスト: TestContainers (PostgreSQL)
- セキュリティテスト: Spring Security Test

### API ドキュメント
アプリケーション起動後、以下のURLでSwagger UIを確認できます:
```
http://localhost:8080/api/swagger-ui.html
```

## プロダクション デプロイ

### Docker
```dockerfile
FROM openjdk:17-jre-slim
COPY build/libs/ams-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 環境設定
- プロダクション環境では `production` プロファイルを使用
- データベース接続プールの設定を最適化
- ログレベルを適切に設定
- ヘルスチェックエンドポイントの監視

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。