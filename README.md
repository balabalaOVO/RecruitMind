# RecruitMind — 智能招聘分析引擎

基于 **Spring Boot 3.2 + Spring AI Alibaba + Vue 3** 的招聘/项目运营 AI Agent，接入阿里云 DashScope 大模型，自动分析候选人简历或项目材料，输出结构化评估结果并持久化。

## 功能

- **三种输入模式**：JSON 结构化提交、纯文本粘贴、多文件上传（.txt）
- **AI 分析输出**：标签、匹配度评分（0-100）、风险点、下一步动作、建议追问
- **历史记录**：分析结果自动存入 MySQL，前端可回溯查看
- **样例演示**：内置 3 份样例材料，一键体验完整分析流程
- **链路追踪**：每次请求分配 traceId，全流程日志可追溯

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.0 |
| AI 引擎 | Spring AI Alibaba + DashScope（deepseek-v4-pro） |
| 数据库 | MySQL 8.0 + Spring Data JPA |
| 前端 | Vue 3（CDN，单文件） |
| 日志 | SLF4J + Logback（文本 + JSON 双输出） |
| 构建 | Maven 3.8+, JDK 17 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 1. 准备数据库

确保 MySQL 运行中。默认配置：

```yaml
# src/main/resources/application.yml
datasource:
  url: jdbc:mysql://localhost:3306/recruitmind?createDatabaseIfNotExist=true
  username: [your username]
  password: [your password]
```

数据库和表由 Hibernate `ddl-auto: update` 自动创建，无需手动建表。

### 2. 配置 API Key

在 `application.yml` 中设置 DashScope API Key：

```yaml
spring:
  ai:
    dashscope:
      api-key: sk-your-api-key
      model: deepseek-v4-pro
```

> 生产环境建议通过环境变量 `DASHSCOPE_API_KEY` 注入。

### 3. 构建与启动

```bash
# 克隆项目
git clone <repo-url>
cd RecruitMind

# 构建
mvn clean package -DskipTests

# 启动
java -jar target/recruitment-agent-1.0.0.jar
```

### 4. 访问

浏览器打开 `http://localhost:8080/`，即可使用前端页面。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/v1/agent/analyze` | JSON 格式批量分析（`Content-Type: application/json`） |
| `POST` | `/api/v1/agent/analyze/text` | 纯文本分析（`Content-Type: text/plain`） |
| `POST` | `/api/v1/agent/analyze/upload` | 文件上传分析（`multipart/form-data`） |
| `GET` | `/api/v1/agent/sample-run` | 一键运行 3 份样例材料分析 |
| `GET` | `/api/v1/agent/history` | 查询历史分析会话列表 |
| `GET` | `/api/v1/agent/history/{id}` | 查询指定会话详情 |

### 请求示例

**JSON 模式**：
```bash
curl -X POST http://localhost:8080/api/v1/agent/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "materials": [
      {"id": "c1", "text": "张三，8年Python后端开发经验，精通Django、FastAPI..."}
    ],
    "jobDescription": "高级Python后端工程师，5年以上经验"
  }'
```

**文件上传**：
```bash
curl -X POST http://localhost:8080/api/v1/agent/analyze/upload \
  -F "files=@candidate1.txt" \
  -F "files=@candidate2.txt"
```

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "jobDescription": "高级Python后端工程师...",
    "totalAnalyzed": 2,
    "results": [
      {
        "inputId": "c1",
        "analysis": {
          "tags": ["Python", "FastAPI", "微服务"],
          "matchScore": 85,
          "risks": [],
          "nextAction": "INTERVIEW",
          "suggestedQuestions": ["请详细描述您主导过的微服务项目"]
        }
      }
    ]
  }
}
```

- **nextAction** 可选值：`INTERVIEW` / `PHONE_SCREEN` / `REJECT` / `NEED_MORE_INFO`
- **matchScore** 范围：0–100

## 项目结构

```
src/
├── main/
│   ├── java/com/example/recruitmentagent/
│   │   ├── RecruitmentAgentApplication.java    # 启动类
│   │   ├── config/                             # 配置（AI、CORS、日志）
│   │   ├── constant/                           # 枚举（NextActionEnum）
│   │   ├── controller/                         # REST 控制器
│   │   ├── dto/                                # 请求/响应 DTO
│   │   ├── entity/                             # JPA 实体
│   │   ├── repository/                         # JPA Repository
│   │   ├── runner/                             # 启动运行器（样例演示）
│   │   ├── service/                            # 业务逻辑
│   │   └── util/                               # 工具类
│   └── resources/
│       ├── application.yml                     # 应用配置
│       ├── logback-spring.xml                  # 日志配置
│       ├── prompts/                            # 提示词模板
│       └── static/index.html                   # Vue 3 前端
└── test/
```

## 数据库表结构

**analysis_session** — 分析会话

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| trace_id | VARCHAR(32) | 请求链路 ID |
| job_description | TEXT | 岗位描述 |
| total_count | INT | 材料总数 |
| success_count | INT | 成功数 |
| fail_count | INT | 失败数 |
| created_at | DATETIME | 创建时间 |

**analysis_result** — 分析结果

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| session_id | BIGINT FK | 关联会话 |
| input_id | VARCHAR(100) | 材料 ID |
| material_text | TEXT | 原始文本 |
| match_score | INT | 匹配度 |
| next_action | VARCHAR(50) | 下一步动作 |
| tags | TEXT(JSON) | 标签列表 |
| risks | TEXT(JSON) | 风险点列表 |
| suggested_questions | TEXT(JSON) | 建议追问列表 |
| error_message | TEXT | 错误信息 |
| created_at | DATETIME | 创建时间 |
