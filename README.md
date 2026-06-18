# MuGood 商城系统

一个完整的全栈式电商商城系统，提供用户端商城、管理后台和后端服务。

## 项目架构

```
mugood-things/
├── MuGood-user/          # 用户端前端 (Vue 3 + Vite)
├── MuGood-admin/         # 管理后台前端 (Vue 3 + Vite)
├── MuGood-server/        # 后端服务 (Spring Boot)
└── sql/                  # 数据库脚本
```

## 技术栈

### 用户端 & 管理后台
- **前端框架**: Vue 3
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI组件**: Element Plus
- **网络请求**: Axios

### 后端服务
- **框架**: Spring Boot
- **持久层**: MyBatis Plus
- **数据库**: MySQL
- **AI集成**: 阿里云 DashScope (通义千问)
- **模板引擎**: Spring AI Tool Calling

## 功能模块

### 用户端 (MuGood-user)
| 模块 | 描述 |
|------|------|
| 首页 | 轮播图、分类导航、新品推荐、热门商品 |
| 商品分类 | 分类浏览、子分类筛选 |
| 商品详情 | 商品信息、SKU选择、规格参数 |
| 购物车 | 购物车管理、数量修改 |
| 结算 | 订单确认、地址选择 |
| 支付 | 支付宝支付集成 |
| 会员中心 | 用户信息、订单管理 |
| 在线客服 | AI 智能客服 |

### 管理后台 (MuGood-admin)
| 模块 | 描述 |
|------|------|
| 仪表盘 | 数据统计概览 |
| 商品管理 | 商品列表、新增、编辑、删除、上下架 |
| 分类管理 | 分类维护 |
| 订单管理 | 订单列表、状态更新 |
| 用户管理 | 用户列表、状态管理 |
| 轮播图管理 | 首页轮播图配置 |

### 后端服务 (MuGood-server)
| 服务 | 描述 |
|------|------|
| HomeService | 首页数据 |
| CatalogService | 分类及商品筛选 |
| GoodsService | 商品详情 |
| CartService | 购物车服务 |
| OrderService | 订单服务 |
| AuthService | 登录认证 |
| AdminService | 后台管理业务 |
| CustomerAgentService | AI 智能客服 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 数据库初始化

```bash
mysql -u root -p < sql/mugood_things.sql
```

### 后端启动

```bash
cd MuGood-server
# 配置数据库连接 (修改 application.yml)
mvn spring-boot:run
```

后端服务默认端口: `8080`

### 用户端启动

```bash
cd MuGood-user
npm install
npm run dev
```

用户端默认端口: `5173`

### 管理后台启动

```bash
cd MuGood-admin
npm install
npm run dev
```

管理后台默认端口: `5174`

## API 接口

### 公共接口
- `GET /home/banner` - 首页轮播图
- `GET /home/category/head` - 顶级分类
- `GET /home/new` - 新品推荐
- `GET /home/hot` - 热门商品
- `GET /goods` - 商品详情
- `GET /category` - 分类信息
- `POST /login` - 用户登录
- `POST /admin/login` - 管理员登录

### 会员接口
- `GET /member/cart` - 购物车列表
- `POST /member/cart` - 添加购物车
- `DELETE /member/cart` - 删除购物车
- `POST /member/order` - 创建订单
- `GET /member/order` - 订单列表

### 管理接口
- `GET /admin/summary` - 数据统计
- `GET /admin/goods` - 商品管理
- `POST /admin/goods` - 新增商品
- `PUT /admin/goods/{id}` - 编辑商品
- `DELETE /admin/goods/{id}` - 删除商品

## 项目结构

### muGood-server 模块

```
muGood-server/
├── muGood-common/         # 公共模块
│   └── com.muGood.common/
│       ├── api/          # 统一响应结构
│       └── exception/    # 业务异常
├── muGood-domain/        # 实体类
│   └── com.muGood.domain.entity/
├── muGood-infrastructure/ # 数据访问层
│   └── com.muGood.infrastructure.mapper/
├── muGood-service/       # 业务逻辑层
│   └── com.muGood.service/
└── muGood-web/            # Web 层
    └── com.muGood.web/
        ├── controller/   # 控制器
        ├── config/       # 配置类
        └── handler/     # 异常处理
```

## License

MIT License
