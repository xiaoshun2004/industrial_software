# KeyTool工具生成自签名证书与管理第三方证书

## 1、创建私钥库的命令：
- （示例）keytool -genkeypair -alias privateKey -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore src/main/resources/license/privateKeys.keystore -storepass public_password1234 -validity 3650 -dname "CN=localhost, OU=localhost, O=localhost, L=SH, ST=SH, C=CN"
- -genkeypair：生成密钥对
- -alias：指定密钥对的别名，主要用于管理
- -keyalg：指定密钥算法，这里使用RSA算法
- -keysize：指定密钥的长度，这里使用2048位
- -sigalg：指定签名算法，这里使用SHA256withRSA
- -keystore：指定密钥库文件的路径
- -storepass：指定密钥库的密码
- -validity：指定密钥对的有效期，这里设置为3650天（10年）
- -dname：指定证书的主题信息，包括CN（通用名称）、OU（组织单位）、O（组织名称）、L（城市）、ST（省份）、C（国家）

## 2.将证书导出为文件，以便在其他系统中使用或共享
- （又是示例）keytool -exportcert -alias "privateKey" -keystore "src/main/resources/privateKeys.keystore" -storepass "public_password1234" -file "src/main/resources/license/certfile.cer"
- -exportcert：导出证书
- -alias：指定要导出的证书的别名
- -keystore：指定密钥库文件的路径
- -storepass：指定密钥库的密码
- -file：指定导出证书文件的路径

## 3.将这个证书文件导入到公钥库中
keytool -import -alias "publicCert" -file "src/main/resources/license/certfile.cer" -keystore "src/main/resources/license/publicCerts.keystore" -storepass "public_password1234"

相关博客链接：
https://llxpbbs.com/archives/wei-ming-ming-wen-zhang