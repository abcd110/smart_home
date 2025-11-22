# 推送命令

网络恢复后，请按顺序执行以下命令：

## 1. 推送 Smart Home Middleware
```bash
cd c:\Users\Administrator\AndroidStudioProjects\mqtt-supabase-middleware
git push -f origin main
```

## 2. 推送 Smart Home Web Simulator
```bash
cd c:\Users\Administrator\AndroidStudioProjects\web-simulator
git push -f origin main
```

## 3. 推送主项目更新
```bash
cd c:\Users\Administrator\AndroidStudioProjects\Smarthome
git push origin main
```

## 项目仓库列表
- Smart Home (主项目): https://github.com/abcd110/smart_home
- Smart Home Web Simulator: https://github.com/abcd110/smart-home-web-simulator  
- Smart Home Middleware: https://github.com/abcd110/smart-home-middleware

## 多仓库架构说明
- 各自独立的技术栈和CI/CD流程
- 清晰的依赖管理和团队协作
- 独立的版本控制和发布管理