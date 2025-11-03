1.后端的部署和执行使用SSH连接服务器
连接配置：
```bash
ssh root@8.134.63.151
密钥：C:\Users\Administrator\AndroidStudioProjects\Smarthome\ecs.pem
``` 
2.后端项目不使用docker-compose部署，直接在服务器上运行。
3.powershell 命令不支持&&连接符，命令请分开执行
4.git提交远程仓库时，不提交文档、以及密钥文件C:\Users\Administrator\AndroidStudioProjects\Smarthome\ecs.pem。
5.每个任务完成后，都在任务清单中标记为完成。
6.当完成的任务涉及到配置，需要在任务清单中记录下配置的内容。